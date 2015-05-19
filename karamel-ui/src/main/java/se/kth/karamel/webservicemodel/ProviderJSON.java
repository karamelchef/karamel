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
public class ProviderJSON {

  public String accessKey;
  public String secretKey;

  public String getAccountId() {
    return accessKey;
  }

  public void setAccountId(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getAccountKey() {
    return secretKey;
  }

  public void setAccountKey(String secretKey) {
    this.secretKey = secretKey;
  }

}
