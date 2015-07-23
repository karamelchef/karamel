/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.github.Github;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.client.model.Ec2;
import se.kth.karamel.client.model.Provider;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 * Authenticated APIs and privacy-sensitive data, that must not be revealed by storing them in the file-system, is 
 * stored here in memory. It is valid just until the system is running otherwise it will disappear.  
 
 * @author kamal
 */
public class ClusterContext {

  private Ec2Context ec2Context;
  private SshKeyPair sshKeyPair;
  private String sudoAccountPassword="";
  private boolean sudoAccountPasswordRequired=false;

  public void setSudoAccountPasswordRequired(boolean sudoAccountPasswordRequired) {
    this.sudoAccountPasswordRequired = sudoAccountPasswordRequired;
  }

  public boolean isSudoAccountPasswordRequired() {
    return sudoAccountPasswordRequired;
  }


  public void setSudoAccountPassword(String sudoAccountPassword) {
    this.sudoAccountPassword = sudoAccountPassword;
  }

  public String getGithubEmail() {
    return Github.getUser();
  }
  
  public String getGithubUsername() {
    return Github.getUser().substring(0, Github.getUser().lastIndexOf("@"));
  }

  public String getGithubPassword() {
    return Github.getPassword();
  }

  public String getSudoAccountPassword() {
    return sudoAccountPassword;
  }

  public String getSudoCommand() {
    return sudoAccountPassword.isEmpty() ? "sudo" : "echo \"" + sudoAccountPassword + "\" | sudo -S ";
  }
  
  public Ec2Context getEc2Context() {
    return ec2Context;
  }

  public void setEc2Context(Ec2Context ec2Context) {
    this.ec2Context = ec2Context;
  }

  public SshKeyPair getSshKeyPair() {
    return sshKeyPair;
  }

  public void setSshKeyPair(SshKeyPair sshKeyPair) {
    this.sshKeyPair = sshKeyPair;
  }

  public void mergeContext(ClusterContext commonContext) {
    if (ec2Context == null) {
      ec2Context = commonContext.getEc2Context();
    }
    if (sshKeyPair == null) {
      sshKeyPair = commonContext.getSshKeyPair();
    }
  }

  public static ClusterContext validateContext(JsonCluster definition,
      ClusterContext context, ClusterContext commonContext) throws KaramelException {
    if (context == null) {
      context = new ClusterContext();
    }
    context.mergeContext(commonContext);

    for (JsonGroup group : definition.getGroups()) {
      Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      if (provider instanceof Ec2 && context.getEc2Context() == null) {
        throw new KaramelException("No valid Ec2 credentials registered :-|");
      }
    }

    if (context.getSshKeyPair() == null) {
      throw new KaramelException("No ssh keypair chosen :-|");
    }
    return context;
  }
}
