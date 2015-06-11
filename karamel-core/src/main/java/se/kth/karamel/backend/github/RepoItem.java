/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.github;

/**
 *
 * @author jdowling
 */
public class RepoItem {

  private final String name;
  private final String description;
  private final String sshUrl;

  public RepoItem(String name, String description, String sshUrl) {
    this.name = name;
    this.description = description;
    this.sshUrl = sshUrl;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public String getSshUrl() {
    return sshUrl;
  }

}
