/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservicemodel;

/**
 *
 * @author jdowling
 */
public class GetClusterDagJSON {

  private final String projectName;

  public GetClusterDagJSON(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectName() {
    return projectName;
  }

}
