package se.kth.karamel.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CookbookScaffolder {

  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CookbookScaffolder.class);

  static boolean mkdirs(String path) {
    File cbDir = new File(path);
    return cbDir.mkdirs();
  }

  static boolean mkFile(String path, StringBuffer contents) throws IOException {
    File f = new File(path);
    return f.createNewFile();
  }

  public static boolean deleteRecursive(File path) throws FileNotFoundException {
    if (!path.exists()) {
      throw new FileNotFoundException(path.getAbsolutePath());
    }
    boolean ret = true;
    if (path.isDirectory()) {
      for (File f : path.listFiles()) {
        ret = ret && deleteRecursive(f);
      }
    }
    return ret && path.delete();
  }

  /**
   *
   * @param path file to be created
   * @param template file used as template from src/resources/main../
   * @param name cookbook name
   * @throws IOException
   */
  public static void createFile(String path, String template, String name) throws IOException {
    String script = IoUtils.readContentFromClasspath(template);
    script = script.replaceAll("%%NAME%%", name);
    String uid = System.getProperty("user.name");
    script = script.replaceAll("%%USER%%", uid);
    // write contents to file as text, not binary data
    PrintWriter out = new PrintWriter(path);
    out.println(script);
    out.flush();
    out.close();
  }

  /**
   * Scaffold a new cookbook with 'name' in the /user/home/.karamel/cookbook_designer/name folder.
   *
   * @param name
   * @return path to newly scaffolded cookbook
   * @throws IOException
   */
  public static String create(String name) throws IOException {
    String cbName = Settings.COOKBOOKS_PATH + File.separator + name + File.separator;

    // Create all the directories for the coookbook 
    mkdirs(cbName + "recipes");
    mkdirs(cbName + "attributes");
    mkdirs(cbName + "experiments");
    mkdirs(cbName + "templates" + File.separator + "default");

    // Create all the files for the coookbook using the file-templates in the resources
    createFile(cbName + Settings.COOKBOOK_DEFAULTRB_REL_PATH, Settings.CB_TEMPLATE_ATTRIBUTES_DEFAULT, name);
    createFile(cbName + Settings.COOKBOOK_BERKSFILE_REL_PATH, Settings.CB_TEMPLATE_BERKSFILE, name);
    createFile(cbName + Settings.COOKBOOK_METADATARB_REL_PATH, Settings.CB_TEMPLATE_METADATA, name);
    createFile(cbName + Settings.COOKBOOK_RECIPE_INSTALL_PATH, Settings.CB_TEMPLATE_RECIPE_INSTALL, name);
    createFile(cbName + Settings.COOKBOOK_README_PATH, Settings.CB_TEMPLATE_README, name);
    logger.debug("Cookbook scaffolding created. Cookbook now in folder: ~/.karamel/cookbooks/" + name);

    File f = new File(cbName);
    return f.getAbsolutePath();
  }

  static public String readFile(String path)
      throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded);
  }

}
