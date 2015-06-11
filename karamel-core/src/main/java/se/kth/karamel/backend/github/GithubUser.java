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
public class GithubUser {
  private String user;
  private String password;

  public GithubUser(String user, String password) {
    this.user = (user == null) ? "" : user;
    this.password = (password == null) ? "" : password;
  }

  public GithubUser() {
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
