package se.kth.karamel.common.clusterdef;

public class Cookbook {

  private String github;
  private String branch;

  public Cookbook() {
  }

  public Cookbook(String github, String branch) {
    this.github = github;
    this.branch = branch;
  }

  public String getGithub() {
    return github;
  }

  public void setGithub(String github) {
    this.github = github;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }
}
