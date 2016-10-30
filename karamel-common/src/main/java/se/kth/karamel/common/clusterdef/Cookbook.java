/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef;

import se.kth.karamel.common.cookbookmeta.CookbookUrls;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class Cookbook {

  private String github;
  private String version;
  private String branch;
  private String cookbook;

  public Cookbook() {
  }

  public Cookbook(Cookbook cookbook) {
    this.github = cookbook.getGithub();
    this.version = cookbook.getVersion();
    this.branch = cookbook.getBranch();
    this.cookbook = cookbook.cookbook;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getGithub() {
    return github;
  }

  public void setGithub(String github) {
    this.github = github;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getCookbook() {
    return cookbook;
  }

  public void setCookbook(String cookbook) {
    this.cookbook = cookbook;
  }
  
  public CookbookUrls getUrls() throws CookbookUrlException {
    CookbookUrls.Builder builder = new CookbookUrls.Builder();
    builder.url(github);
    if (branch != null)
      builder.branchOrVersion(branch);
    else if (version != null)
      builder.branchOrVersion(version);
    if (cookbook != null)
      builder.cookbookRelPath(cookbook);
    CookbookUrls urls = builder.build();
    return urls;
  }
}
