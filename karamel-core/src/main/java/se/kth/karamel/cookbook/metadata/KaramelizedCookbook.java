/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
  private List<ExperimentRecipe> experimentRecipes = new ArrayList<>();
  private InstallRecipe installRecipe;
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

    List<Recipe> recipes = this.metadataRb.getRecipes();
    for (Recipe r : recipes) {
      String name = r.getName();
      if (name == null || name.isEmpty()) {
        throw new MetadataParseException("Invalid recipe name in metadata.rb");
      }
      String[] recipeData = r.getName().split("::");
      if (recipeData.length < 2) {
        throw new MetadataParseException("Invalid recipe name in metadata.rb. Name should be- cookbook::recipe");
      }
      String experimentName = recipeData[1] + ".rb";
      String description = r.getDescription();
      String[] configData = description.split(",configFile=");
      String configFileName = "";
      String configFileContents = "";
      String experimentContent;
      if (configData.length > 1) {
        String desc = configData[1];
        int pos = desc.indexOf(";");
        if (pos != -1) {
          configFileName = desc.substring(0, pos - 1);
          int pathPos = configFileName.lastIndexOf("/");
          if (pathPos != -1) {
            configFileName = configFileName.substring(pathPos + 1);
          }
        }
      }
      if (!configFileName.isEmpty()) {
        String configFileUrl = urls.rawHome + File.separator + "templates" + File.separator
            + "default" + File.separator + configFileName + ".erb";
        try {
          configFileContents = IoUtils.readContent(configFileUrl);
        } catch (IOException ex) {
          Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO, "Not found in this cookbook: "
              + urls.recipesHome + experimentName, ex);
        }
      }

      ExperimentRecipe er = null;
      try {
        experimentContent = IoUtils.readContent(urls.recipesHome + experimentName);
        er = ExperimentRecipeParser.parse(r.getName(), experimentContent, configFileName, configFileContents);
      } catch (IOException ex) {
        Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO, "Not found in this cookbook: "
            + urls.recipesHome + experimentName, ex);
      } catch (RecipeParseException ex) {
        Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO,
            "Experiment recipe not a valid format in this cookbook: "
            + urls.recipesHome + experimentName, ex);
      }

      if (er != null) {
        experimentRecipes.add(er);
      }

    }

    try {
      String installContent = IoUtils.readContent(urls.recipesHome + "install.rb");
      this.installRecipe = InstallRecipeParser.parse(installContent);
    } catch (IOException ex) {
      Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO, "Not found in this cookbook: "
          + urls.recipesHome + "install.rb", ex);
    } catch (RecipeParseException ex) {
      Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO,
          "Install recipe not a valid format in this cookbook: "
          + urls.recipesHome + "install.rb", ex);
    }
  }

  public Berksfile getBerksFile() {
    return berksFile;
  }

  public String getMetadataJson() {
    if (json == null) {
      GsonBuilder builder = new GsonBuilder();
      builder.disableHtmlEscaping();
      Gson gson = builder.setPrettyPrinting().create();
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

  public List<ExperimentRecipe> getExperimentRecipes() {
    return experimentRecipes;
  }

  public InstallRecipe getInstallRecipe() {
    return installRecipe;
  }

  public DefaultRb getDefaultRb() {
    return defaultRb;
  }

}
