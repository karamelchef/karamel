package se.kth.karamel.webservicemodel;

import java.util.ArrayList;

public class CookbooksJSON {

  private String cookbookName;
  private String repo;
  private ArrayList<String> mandatoryAttrs;
  private ArrayList<String> optionalAttrs;

  public CookbooksJSON(String cookbookName, String repo, ArrayList<String> mandatoryAttrs,
      ArrayList<String> optionalAttrs) {
    this.cookbookName = cookbookName;
    this.repo = repo;
    this.mandatoryAttrs = mandatoryAttrs;
    this.optionalAttrs = optionalAttrs;
  }

}
