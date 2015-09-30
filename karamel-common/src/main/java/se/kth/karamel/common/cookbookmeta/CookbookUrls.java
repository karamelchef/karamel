/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import java.util.regex.Matcher;
import se.kth.karamel.common.util.Settings;
import static se.kth.karamel.common.util.Settings.CB_CLASSPATH_MODE;
import static se.kth.karamel.common.util.Settings.COOKBOOKS_PATH;
import static se.kth.karamel.common.util.Settings.COOKBOOK_BERKSFILE_REL_URL;
import static se.kth.karamel.common.util.Settings.COOKBOOK_CONFIGFILE_REL_URL;
import static se.kth.karamel.common.util.Settings.COOKBOOK_DEFAULTRB_REL_URL;
import static se.kth.karamel.common.util.Settings.COOKBOOK_KARAMELFILE_REL_URL;
import static se.kth.karamel.common.util.Settings.COOKBOOK_METADATARB_REL_URL;
import static se.kth.karamel.common.util.Settings.GITHUB_BASE_URL;
import static se.kth.karamel.common.util.Settings.GITHUB_RAW_URL;
import static se.kth.karamel.common.util.Settings.GITHUB_REPO_NO_BRANCH_PATTERN;
import static se.kth.karamel.common.util.Settings.GITHUB_REPO_WITH_BRANCH_PATTERN;
import static se.kth.karamel.common.util.Settings.GITHUB_REPO_WITH_SUBCOOKBOOK_PATTERN;
import static se.kth.karamel.common.util.Settings.REPO_NO_BRANCH_PATTERN;
import static se.kth.karamel.common.util.Settings.REPO_WITH_BRANCH_PATTERN;
import static se.kth.karamel.common.util.Settings.REPO_WITH_SUBCOOKBOOK_PATTERN;
import static se.kth.karamel.common.util.Settings.SLASH;
import static se.kth.karamel.common.util.Settings.TEST_CB_ROOT_FOLDER;
import static se.kth.karamel.common.util.Settings.USE_CLONED_REPO_FILES;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class CookbookUrls {

  public String orgName;
  public String repoName;
  public String orgRepo;
  public String repoUrl;
  public String branch;
  public String cookbookRelPath;
  public String id;
  public String cookbookUrl;
  public String cookbookRawUrl;
  public String attrFile;
  public String metadataFile;
  public String karamelFile;
  public String berksFile;
  public String configFile;
  public String recipesHome;

  public CookbookUrls(String orgName, String repoName, String orgRepo, String repoUrl, String branch,
      String cookbookRelPath, String id, String home, String rawHome, String attrFile, String metadataFile,
      String karamelFile, String berksFile, String configFile, String recipesHome) {
    this.orgName = orgName;
    this.repoName = repoName;
    this.orgRepo = orgRepo;
    this.repoUrl = repoUrl;
    this.branch = branch;
    this.cookbookRelPath = cookbookRelPath;
    this.id = id;
    this.cookbookUrl = home;
    this.cookbookRawUrl = rawHome;
    this.attrFile = attrFile;
    this.metadataFile = metadataFile;
    this.karamelFile = karamelFile;
    this.berksFile = berksFile;
    this.configFile = configFile;
    this.recipesHome = recipesHome;
  }

  public static class Builder {

    String id;
    String url;
    String repo;
    String org;
    String branch;
    String cookbookRelPath;

    /**
     *
     * @param url url to reposiory name if files == false. Otherwise the name of the reo
     * @return
     * @throws CookbookUrlException
     */
    public Builder url(String url) throws CookbookUrlException {
      if (url.isEmpty()) {
        throw new CookbookUrlException("Cookbook url is empty.");
      }
      this.url = url.trim();

      return this;
    }

    public Builder branchOrVersion(String branch) {
      this.branch = branch;
      return this;
    }

    public Builder cookbookRelPath(String subCookbook) {
      this.cookbookRelPath = subCookbook;
      return this;
    }

    public CookbookUrls buildById(String url) throws CookbookUrlException {
      if (url.isEmpty()) {
        throw new CookbookUrlException("Cookbook id is empty.");
      }
      this.id = id.trim();

      return build();
    }

    public CookbookUrls build() throws CookbookUrlException {
      if (id != null) {
        //id based data extraction
        Matcher matcher = REPO_WITH_SUBCOOKBOOK_PATTERN.matcher(id);
        if (matcher.matches()) {
          org = matcher.group(1);
          repo = matcher.group(2);
          branch = matcher.group(3);
          cookbookRelPath = matcher.group(4);
        } else {
          throw new CookbookUrlException(String.format("'%s' is not a valid cookbook id, it must be the following "
              + "format: \n'http(s)://github.com/<org>/<repo>/tree/<branch or version>/<cookbook-relative-path>'", id));
        }
      } else {
        //url based data extraction
        boolean found = false;
        Matcher matcher = REPO_WITH_SUBCOOKBOOK_PATTERN.matcher(url);
        if (matcher.matches()) {
          found = true;
        }

        if (!found) {
          matcher = GITHUB_REPO_WITH_SUBCOOKBOOK_PATTERN.matcher(url);
          if (matcher.matches()) {
            found = true;
          }
        }

        if (found) {
          cookbookRelPath = matcher.group(4);
        }

        if (!found) {
          matcher = REPO_WITH_BRANCH_PATTERN.matcher(url);
          if (matcher.matches()) {
            found = true;
          }
        }

        if (!found) {
          matcher = GITHUB_REPO_WITH_BRANCH_PATTERN.matcher(url);
          if (matcher.matches()) {
            found = true;
          }
        }

        if (found && branch == null) {
          branch = matcher.group(3);
        }

        if (!found) {
          matcher = REPO_NO_BRANCH_PATTERN.matcher(url);
          if (matcher.matches()) {
            found = true;
          }
        }

        if (!found) {
          matcher = GITHUB_REPO_NO_BRANCH_PATTERN.matcher(url);
          if (matcher.matches()) {
            found = true;
          }
        }

        if (found) {
          org = matcher.group(1);
          repo = matcher.group(2);
          if (branch == null) {
            branch = Settings.GITHUB_DEFAULT_BRANCH;
          }
        } else {
          throw new CookbookUrlException(String.format("'%s' is not a valid Github url, it must be one the following "
              + "formats: \n'http(s)://github.com/<org>/<repo>', "
              + "\n'http(s)://github.com/<org>/<repo>/tree/<branch>', "
              + "\n'http(s)://github.com/<org>/<repo>/tree/<branch>/<cookbook-relative-path>', "
              + "\n'<org>/<repo>', \n'<org>/<repo>/tree/<branch>',"
              + " or \n'<org>/<repo>/tree/<branch>/<cookbook-relative-path>'", url));
        }
      }

      String base = CB_CLASSPATH_MODE ? TEST_CB_ROOT_FOLDER : GITHUB_BASE_URL;
      String raw = CB_CLASSPATH_MODE ? TEST_CB_ROOT_FOLDER : GITHUB_RAW_URL;

      id = GITHUB_BASE_URL + SLASH + org + SLASH + repo + SLASH + "tree" + SLASH + branch;
      String home = base + SLASH + org + SLASH + repo;
      String repoHome = base + SLASH + org + SLASH + repo;
      String rawHome = raw + SLASH + org + SLASH + repo + SLASH + branch;

      if (cookbookRelPath != null && !cookbookRelPath.isEmpty()) {
        String subPath = SLASH + cookbookRelPath;
        id += subPath;
        home += subPath;
        rawHome += subPath;
      }

      if (USE_CLONED_REPO_FILES) {
        rawHome = COOKBOOKS_PATH + SLASH + repo;
      }

      String attrFile = rawHome + COOKBOOK_DEFAULTRB_REL_URL;
      String metadataFile = rawHome + COOKBOOK_METADATARB_REL_URL;
      String karamelFile = rawHome + COOKBOOK_KARAMELFILE_REL_URL;
      String berksFile = rawHome + COOKBOOK_BERKSFILE_REL_URL;
      String configFile = rawHome + COOKBOOK_CONFIGFILE_REL_URL;
      String recipesHome = rawHome + "/recipes/";
      CookbookUrls urls = new CookbookUrls(org, repo, org + SLASH + repo, repoHome, branch, cookbookRelPath, id, home,
          rawHome, attrFile, metadataFile, karamelFile, berksFile, configFile, recipesHome);
      return urls;
    }

  }
}
