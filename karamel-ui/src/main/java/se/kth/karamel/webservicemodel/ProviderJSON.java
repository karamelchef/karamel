package se.kth.karamel.webservicemodel;

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
