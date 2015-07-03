/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.karamel.common.IoUtils;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.RecipeParseException;

/**
 * Represents a coobook located in github
 *
 * @author kamal
 */
public class KaramelizedCookbook {

  private final CookbookUrls urls;
  private final DefaultRb defaultRb;
  private final MetadataRb metadataRb;
  private final KaramelFile karamelFile;
  private final Berksfile berksFile;
  private String configFile;
  private ExperimentRecipe experimentRecipe;
  private String json;

  public KaramelizedCookbook(String homeUrl) throws CookbookUrlException, MetadataParseException {
    CookbookUrls.Builder builder = new CookbookUrls.Builder();
    this.urls = builder.url(homeUrl).build();
    try {
      List<String> lines1 = IoUtils.readLines(urls.attrFile);
      this.defaultRb = new DefaultRb(lines1);
      String metadataContent = IoUtils.readContent(urls.metadataFile);
      this.metadataRb = MetadataParser.parse(metadataContent);
      this.metadataRb.setDefaults(defaultRb);
      String karamelFileContent = IoUtils.readContent(urls.karamelFile);
      this.karamelFile = new KaramelFile(karamelFileContent);
      List<String> berksfileLines = IoUtils.readLines(urls.berksFile);
      this.berksFile = new Berksfile(berksfileLines);
    } catch (IOException e) {
      throw new CookbookUrlException("", e);
    }
    // Subsequent files may not be found in the cookbook, and that's ok. No need to throw an exception.
    try {
      this.configFile = IoUtils.readContent(urls.configFile);
    } catch (IOException ex) {
      Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO, "Not found in this cookbook: "
          + urls.configFile, ex);
    }
    try {
      String recipeContents = IoUtils.readContent(urls.experimentRecipe);
      this.experimentRecipe = ExperimentRecipeParser.parse(recipeContents);
    } catch (IOException ex) {
      Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO, "Not found in this cookbook: "
          + urls.experimentRecipe, ex);
    } catch (RecipeParseException ex) {
      Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO,
          "Experiment recipe not a valid format in this cookbook: "
          + urls.experimentRecipe, ex);
    }

  }

  public String getConfigFile() {
    return configFile;
  }

  public Berksfile getBerksFile() {
    return berksFile;
  }

  public String getMetadataJson() {
    if (json == null) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      json = gson.toJson(metadataRb);
    }
    return json;
  }

  public MetadataRb getMetadataRb() {
    return metadataRb;
  }

  public KaramelFile getKaramelFile() {
    return karamelFile;
  }

  public ExperimentRecipe getExperimentRecipe() {
    return experimentRecipe;
  }

  public DefaultRb getDefaultRb() {
    return defaultRb;
  }

}
