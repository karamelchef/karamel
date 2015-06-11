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
public class OrgItem {
  private String name;
  private String gravitar;

  public OrgItem(String name, String gravitar) {
    this.name = name;
    this.gravitar = gravitar;
  }

  public String getGravitar() {
    return gravitar;
  }

  public String getName() {
    return name;
  }
  
}
