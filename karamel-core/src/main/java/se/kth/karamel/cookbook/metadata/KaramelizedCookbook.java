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
import se.kth.karamel.common.Settings;
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
      String experimentFilename = recipeData[1] + ".rb";
      String description = r.getDescription();
      String searchStr = "configFile=";
      int confPos = description.indexOf(searchStr);
      String configFileName = "";
      String configFileContents = "";
      String experimentContent;
      if (confPos != -1 && confPos < description.length() + searchStr.length()) {
        String desc = description.substring(confPos + searchStr.length());
        int pos = desc.indexOf(";");
        if (pos != -1) {
          configFileName = desc.substring(0, pos);
          int pathPos = configFileName.lastIndexOf("/");
          if (pathPos != -1) {
            configFileName = configFileName.substring(pathPos + 1);
          }
        }
      }
      if (!configFileName.isEmpty()) {
        String configFileUrl = urls.rawHome + File.separator + "templates" + File.separator
            + "defaults" + File.separator + configFileName + ".erb";
        try {
          configFileContents = IoUtils.readContent(configFileUrl);
        } catch (IOException ex) {
          Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO, "Not found in this cookbook: "
              + urls.recipesHome + experimentFilename, ex);
        }
      }

      ExperimentRecipe er = null;
      try {
        // Only parse experiment recipes here, parse the install.rb recipe later.
        if (experimentFilename.compareTo(Settings.INSTALL_RECIPE + ".rb") != 0) {
          experimentContent = IoUtils.readContent(urls.recipesHome + experimentFilename);
          er = ExperimentRecipeParser.parse(r.getName(), experimentContent, configFileName, configFileContents);
        }
      } catch (IOException ex) {
        Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO, "Not found in this cookbook: "
            + urls.recipesHome + experimentFilename, ex);
      } catch (RecipeParseException ex) {
        Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO,
            "Experiment recipe not a valid format in this cookbook: "
            + urls.recipesHome + experimentFilename, ex);
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
      throw new CookbookUrlException(
          "Could not download recipes/install.rb. Does the file exist? Is the Internet working? " + ex.getMessage());
    } catch (RecipeParseException ex) {
      Logger.getLogger(KaramelizedCookbook.class.getName()).log(Level.INFO,
          "Install recipe not a valid format in this cookbook: "
          + urls.recipesHome + "install.rb", ex);
      throw new MetadataParseException("Install recipe not a valid format in this cookbook: "
          + urls.recipesHome + "install.rb . " + ex.getMessage());
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
