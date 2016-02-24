package se.kth.karamel.common.util;

/**
 * Nova Credentials
 * Created by Alberto on 2015-05-16.
 */
public class NovaCredentials {
  private String accountName;
  private String accountPass;
  private String endpoint;
  private String region;

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public String getAccountPass() {
    return accountPass;
  }

  public void setAccountPass(String accountPass) {
    this.accountPass = accountPass;
  }
}
