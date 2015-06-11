/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservicemodel;

import java.util.List;

/**
 *
 * @author jdowling
 */
public class GithubReposJSON {

  private String org;
  private List<String> repos;

  public GithubReposJSON(String org, List<String> repos) {
    this.org = org;
    this.repos = repos;
  }

  public String getOrg() {
    return org;
  }

  public List<String> getRepos() {
    return repos;
  }

  public void setOrg(String org) {
    this.org = org;
  }

  public void setRepos(List<String> repos) {
    this.repos = repos;
  }

}
