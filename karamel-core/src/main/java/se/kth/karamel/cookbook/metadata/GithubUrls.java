/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import se.kth.karamel.common.Settings;
import static se.kth.karamel.common.Settings.COOKBOOK_DEFAULTRB_REL_PATH;
import static se.kth.karamel.common.Settings.COOKBOOK_KARAMELFILE_REL_PATH;
import static se.kth.karamel.common.Settings.COOKBOOK_METADATARB_REL_PATH;
import static se.kth.karamel.common.Settings.GITHUB_BASE_URL;
import static se.kth.karamel.common.Settings.GITHUB_DEFAULT_BRANCH;
import static se.kth.karamel.common.Settings.GITHUB_RAW_URL;
import static se.kth.karamel.common.Settings.GITHUB_REPO_NO_BRANCH_PATTERN;
import static se.kth.karamel.common.Settings.GITHUB_REPO_WITH_BRANCH_PATTERN;
import static se.kth.karamel.common.Settings.REPO_NO_BRANCH_PATTERN;
import static se.kth.karamel.common.Settings.REPO_WITH_BRANCH_PATTERN;
import static se.kth.karamel.common.Settings.SLASH;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class GithubUrls {

  public String repoName;
  public String branch;
  public String id;
  public String home;
  public String rawHome;
  public String attrFile;
  public String metadataFile;
  public String karamelFile;
  public String berksfile;

  public GithubUrls(String repoName, String branch, String id, String home, String rawHome, String attrFile, 
          String metadataFile, String karamelFile, String berksfile) {
    this.repoName = repoName;
    this.branch = branch;
    this.id = id;
    this.home = home;
    this.rawHome = rawHome;
    this.attrFile = attrFile;
    this.metadataFile = metadataFile;
    this.karamelFile = karamelFile;
    this.berksfile = berksfile;
  }

  public static class Builder {

    String url;
    String repo;
    String user;
    String branch;

    public Builder url(String url) throws CookbookUrlException {
      if (url.isEmpty()) {
        throw new CookbookUrlException("Github url is null.");
      }
      this.url = url.trim();
      return this;
    }

    public Builder branchOrVersion(String branch) {
      this.branch = branch;
      return this;
    }

    public GithubUrls build() throws CookbookUrlException {
      if (url.matches(REPO_WITH_BRANCH_PATTERN) || url.matches(GITHUB_REPO_WITH_BRANCH_PATTERN)) {
        String[] comp = url.split(SLASH);
        user = comp[comp.length - 4];
        repo = comp[comp.length - 3];
        if (branch == null) {
          branch = comp[comp.length - 1];
        }
      } else if (url.matches(REPO_NO_BRANCH_PATTERN) || url.matches(GITHUB_REPO_NO_BRANCH_PATTERN)) {
        String[] comp = url.split(SLASH);
        user = comp[comp.length - 2];
        repo = comp[comp.length - 1];
        if (branch == null) {
          branch = GITHUB_DEFAULT_BRANCH;
        }
      } else {
        throw new CookbookUrlException(String.format("'%s' is not a valid Github url, it must be one the following formats: \n'http(s)://github.com/<user_name>/<repo>', \n'http(s)://github.com/<user_name>/<repo>/tree/<branch>', \n'<user_name>/<repo>', or \n'<user_name>/<repo>/tree/<branch>'", url));
      }

      String id = GITHUB_BASE_URL + SLASH + user + SLASH + repo + SLASH + "tree" + SLASH + branch;
      String home = GITHUB_BASE_URL + SLASH + user + SLASH + repo;
      String rawHome = GITHUB_RAW_URL + SLASH + user + SLASH + repo + SLASH + branch;

      String attrFile = rawHome + COOKBOOK_DEFAULTRB_REL_PATH;
      String metadataFile = rawHome + COOKBOOK_METADATARB_REL_PATH;
      String karamelFile = rawHome + COOKBOOK_KARAMELFILE_REL_PATH;
      String berksFile = rawHome + Settings.COOKBOOK_BERKSFILE_REL_PATH;
      GithubUrls urls = new GithubUrls(repo, branch, id, home, rawHome, attrFile, metadataFile, karamelFile, berksFile);
      return urls;
    }

  }
}
