/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservicemodel;

public class CookbookParamsJSON {

  private final String cluster;
  private final String cookbook;

  public CookbookParamsJSON(String cluster, String cb) {
    this.cluster = cluster;
    this.cookbook = cb;
  }

  public String getCookbook() {
    return cookbook;
  }

  public String getCluster() {
    return cluster;
  }

}
