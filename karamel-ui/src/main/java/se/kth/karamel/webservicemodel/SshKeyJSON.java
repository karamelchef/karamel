package se.kth.karamel.webservicemodel;

/**
 * JSON representing the passphrase for the user's ssh private key.
 *
 */
public class SshKeyJSON {

  private String privKeyPath;
  private String pubKeyPath;
  private String passphrase;

  public SshKeyJSON() {
  }

  public SshKeyJSON(String privKeyPath, String pubKeyPath, String passphrase) {
    this.passphrase = passphrase;
    this.privKeyPath = privKeyPath;
    this.pubKeyPath = pubKeyPath;
  }

  public void setPrivKeyPath(String privKeyPath) {
    this.privKeyPath = privKeyPath;
  }

  public void setPubKeyPath(String pubKeyPath) {
    this.pubKeyPath = pubKeyPath;
  }

  public String getPrivKeyPath() {
    return privKeyPath;
  }

  public String getPubKeyPath() {
    return pubKeyPath;
  }

  public String getPassphrase() {
    return passphrase;
  }

  public void setPassphrase(String passphrase) {
    this.passphrase = passphrase;
  }

}
