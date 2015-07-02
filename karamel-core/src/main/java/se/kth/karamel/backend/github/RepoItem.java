package se.kth.karamel.backend.github;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RepoItem {

  private String name;
  private String description;
  private String sshUrl;

  public RepoItem(String name, String description, String sshUrl) {
    this.name = name;
    this.description = description;
    this.sshUrl = sshUrl;
  }

  public RepoItem() {
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

  public void setDescription(String description) {
    this.description = description;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSshUrl(String sshUrl) {
    this.sshUrl = sshUrl;
  }

}
