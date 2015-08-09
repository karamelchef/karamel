package se.kth.karamel.webservicemodel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JSON representing the new cookbook to be created.
 *
 */
@XmlRootElement
public class GithubCredentialsJSON {

  private String email;
  private String password;

  public GithubCredentialsJSON() {
  }

  public GithubCredentialsJSON(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
