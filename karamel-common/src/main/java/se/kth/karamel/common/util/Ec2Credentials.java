/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.common.util;

/**
 *
 * @author kamal
 */
public class Ec2Credentials {
  
  private String accessKey="";
  private String secretKey="";

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String accessKey) {
    this.secretKey = accessKey;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }
  
}
