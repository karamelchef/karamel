package se.kth.karamel.webservicemodel;

/**
 * Created by Alberto on 2015-08-16.
 */
public class NovaJSON {
  private String accountName;
  private String accountPass;
  private String endpoint;
  private String region;
  private String networkId;

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

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }
  
  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }
}
