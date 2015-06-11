/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservicemodel;

import se.kth.karamel.backend.github.OrgItem;
import java.util.List;

/**
 *
 * @author jdowling
 */
public class GithubOrgsJSON {

  

  List<OrgItem> orgs;

  public List<OrgItem> getOrgs() {
    return orgs;
  }

  public void setOrgs(List<OrgItem> orgs) {
    this.orgs = orgs;
  }
  
}
