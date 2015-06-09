package se.kth.karamel.webservicemodel;

/**
 * JSON representing the password for a sudo account 
 *
 */
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
