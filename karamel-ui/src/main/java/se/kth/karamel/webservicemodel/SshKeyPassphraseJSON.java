package se.kth.karamel.webservicemodel;

/**
 * JSON representing the passphrase for the user's ssh private key.
 *
 */
public class SshKeyPassphraseJSON {

    private String passphrase;

    public SshKeyPassphraseJSON(){}

    public SshKeyPassphraseJSON(String json){
        this.passphrase = json;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

}
