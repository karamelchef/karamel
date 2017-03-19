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
public class SshKeyPair {

  private String privateKeyPath;
  private String publicKeyPath;
  private String privateKey;
  private String publicKey;
  private String passphrase;
  private boolean needsPassword;

  public boolean isNeedsPassword() {
    return needsPassword;
  }

  public void setNeedsPassword(boolean needsPassword) {
    this.needsPassword = needsPassword;
  }
  
  public String getPassphrase() {
    return (passphrase != null && passphrase.isEmpty()) ? null : passphrase;
  }

  public void setPassphrase(String password) {
    this.passphrase = password;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getPrivateKeyPath() {
    return privateKeyPath;
  }

  public void setPrivateKeyPath(String privateKeyPath) {
    this.privateKeyPath = privateKeyPath;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getPublicKeyPath() {
    return publicKeyPath;
  }

  public void setPublicKeyPath(String publicKeyPath) {
    this.publicKeyPath = publicKeyPath;
  }

}
