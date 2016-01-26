package se.kth.karamel.webservicemodel;

/**
 * Created by Mamut on 2016-01-18.
 */
public class OcciJSON {
  private String userCertificatePath;
  private String systemCertDir;
  //private String endpoint;
    
  public String getUserCertificatePath() {
    return userCertificatePath;
  }

  public void setUserCertificatePath(String userCertificatePath) {
    this.userCertificatePath = userCertificatePath;
  }

  public String getSystemCertDir() {
    return systemCertDir;
  }

  public void setSystemCertDir(String systemCertDir) {
    this.systemCertDir = systemCertDir;
  }

  /*public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }*/

}
