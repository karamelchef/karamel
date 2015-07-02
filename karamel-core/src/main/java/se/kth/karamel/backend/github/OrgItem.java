package se.kth.karamel.backend.github;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrgItem {
  private String name;
  private String gravitar;

  public OrgItem(String name, String gravitar) {
    this.name = name;
    this.gravitar = gravitar;
  }

  public OrgItem() {
  }

  public String getGravitar() {
    return gravitar;
  }

  public String getName() {
    return name;
  }

  public void setGravitar(String gravitar) {
    this.gravitar = gravitar;
  }

  public void setName(String name) {
    this.name = name;
  }
  
}
