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

  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(KaramelizedCookbook.class);

  private final CookbookUrls urls;
  private final DefaultRb defaultRb;
  private final MetadataRb metadataRb;
  private final KaramelFile karamelFile;
  private final Berksfile berksFile;
  private List<ExperimentRecipe> experimentRecipes = new ArrayList<>();
  private InstallRecipe installRecipe;
  private String json;

  /**
   *
   * @param homeUrl url or canonical path to the cookbook
   * @param local true if it is a canonical path (to a cloned cookbook) and not a URL.
   * @throws CookbookUrlException
   * @throws MetadataParseException
   */
  public KaramelizedCookbook(String homeUrl, boolean local) throws CookbookUrlException, MetadataParseException {
    if (local) {
      Settings.USE_CLONED_REPO_FILES = true;
    }
    CookbookUrls.Builder builder = new CookbookUrls.Builder();
    this.urls = builder.url(homeUrl).build();
    try {
      List<String> defaultsLines = IoUtils.readLines(urls.attrFile);
      this.defaultRb = new DefaultRb(defaultsLines);
      String metadataContent = IoUtils.readContent(urls.metadataFile);
      this.metadataRb = MetadataParser.parse(metadataContent);
      this.metadataRb.setDefaults(defaultRb);
      String karamelFileContent = IoUtils.readContent(urls.karamelFile);
      this.karamelFile = new KaramelFile(karamelFileContent);
      List<String> berksfileLines = IoUtils.readLines(urls.berksFile);
      this.berksFile = new Berksfile(berksfileLines);
    } catch (IOException e) {
      Settings.USE_CLONED_REPO_FILES = false;
      throw new CookbookUrlException("", e);
    }

    List<Recipe> recipes = this.metadataRb.getRecipes();
    for (Recipe r : recipes) {
      String name = r.getName();
      if (name == null || name.isEmpty()) {
        throw new MetadataParseException("Invalid recipe name in metadata.rb");
      }
      String[] recipeData = r.getName().split(Settings.COOKBOOK_DELIMITER);
      // assume recipe name is 'default'
      String experimentFilename = "default.rb";
      if (recipeData.length > 1) {
        experimentFilename = recipeData[1] + ".rb";
      }
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
          logger.info("Not found in this cookbook: " + urls.recipesHome + experimentFilename, ex);
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
        logger.debug("This cookbook does not have a karamelized experiment: " + urls.recipesHome + experimentFilename
            + " - " + ex.getMessage());
      } catch (RecipeParseException ex) {
        logger.warn("The recipe is not in a karamelized format: " + urls.recipesHome + experimentFilename
            + " - " + ex.getMessage());
      }

      if (er != null) {
        experimentRecipes.add(er);
      }

    }

    try {
      String installContent = IoUtils.readContent(urls.recipesHome + "install.rb");
      this.installRecipe = InstallRecipeParser.parse(installContent);
    } catch (IOException ex) {
      throw new CookbookUrlException(
          "Could not find the file 'recipes/install.rb'. Does the file exist? Is the Internet working?");
    } catch (RecipeParseException ex) {
      logger.warn("Install recipe not in a format that can be used by Karamel Experiments: "
          + urls.recipesHome + "install.rb" + " - " + ex.getMessage());

    } finally {
      Settings.USE_CLONED_REPO_FILES = false;
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
