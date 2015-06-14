/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.github.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.backend.ExperimentContext;
import se.kth.karamel.backend.github.Github;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author jdowling
 */
public class ChefExperimentExtractor {

  /**
   * Parses all scripts and config files and outputs to metadata.rb and attributes/default.rb the configuration values
   * found.
   *
   * @param owner org/user on github
   * @param repoName name of github repository
   * @param experiment input scripts/config filenames and content
   * @throws KaramelException
   */
  public static void firstPassAttributeParsing(String owner, String repoName, ExperimentContext experiment)
      throws KaramelException {

    // <ParamName, ParamValue> 
    Map<String, String> defaultAttrs = new HashMap<>();
    Map<String, String> metadataAttrs = new HashMap<>();

    // Parse all the config variables and put them into attributes/default.rb
    Map<String, String> configFiles = experiment.getConfigFiles();
    for (String configFile : configFiles.keySet()) {
      String str = configFiles.get(configFile);
      Pattern p = Pattern.compile("%%(.*)%%\\s*=\\s*(.*)\\s*");
      Matcher m = p.matcher(str);
      while (m.find()) {
        String matched = m.group();
        String name = m.group(1);
        String value = m.group(2);
        defaultAttrs.put(name, value);
        metadataAttrs.put(name, value);
      }
    }

    // Parse all the params defined in scripts and put them into metadata.rb
    Map<String, String> scripts = experiment.getScripts();
    for (String script : scripts.keySet()) {
      String str = scripts.get(script);
      Pattern p = Pattern.compile("%%[--]*[-D]*(.*)%%\\s*=\\s*(.*)[\\s]+");
      Matcher m = p.matcher(str);
      while (m.find()) {
        String name = m.group(1);
        String value = m.group(2);
        defaultAttrs.put(name, value);
        metadataAttrs.put(name, value);
      }
    }

    // 2. write them to attributes/defaults.rb and metadata.rb
    
    String email = (Github.getEmail() == null) ? "karamel@karamel.io" : Github.getEmail();
    try {
      StringBuilder defaults_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_ATTRIBUTES_DEFAULT,
          "name", repoName,
          "user", experiment.getUser(),
          "group", experiment.getGroup(),
          "http_binaries", experiment.getUrl()
      );
      
      for (String key : defaultAttrs.keySet()) {
        String entry = "default[:" + repoName + "][:" + key + "] = \"" + defaultAttrs.get(key) + "\"";
        defaults_rb.append(System.lineSeparator()).append(entry).append(System.lineSeparator());
      }
      
      StringBuilder metadata_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_METADATA,
          "name", repoName,
          "user", owner,
          "email", email
      );
      for (String key : metadataAttrs.keySet()) {
        String entry = "attribute " + repoName + "/" + key + "\"" + System.lineSeparator()
            + ":description => \"" + key + " parameter value\"," + System.lineSeparator()
            + ":type => \"string\"";
        defaults_rb.append(System.lineSeparator()).append(entry).append(System.lineSeparator());
      }
      
      // 3. write them to files and push to github
      
      Github.addCommitPushFile(owner, repoName, "attributes/defaults.rb" , defaults_rb.toString());      
      Github.addCommitPushFile(owner, repoName, "metadata.rb" , metadata_rb.toString());      
      
    } catch (IOException ex) {
      Logger.getLogger(ChefExperimentExtractor.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    }
  }
}
