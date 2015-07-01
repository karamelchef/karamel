package se.kth.karamel.backend.github;

public class GithubUser {
  private String user;
  private String password;
  private String email;

  public GithubUser(String user, String password, String email) {
    this.user = (user == null) ? "" : user;
    this.password = (password == null) ? "" : password;
    this.email = (email == null) ? "" : email;
  }

  public GithubUser() {
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
 
  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public void setUser(String email) {
    this.user = email;
  }

  public void setPassword(String password) {
    this.password = password;
  }
  
}
