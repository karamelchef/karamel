package se.kth.karamel.webservicemodel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JSON representing the password for a sudo account 
 *
 */
@XmlRootElement
public class SudoPasswordJSON {

  private String password;

  public SudoPasswordJSON() {
  }

  public SudoPasswordJSON(String password) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
  
}
