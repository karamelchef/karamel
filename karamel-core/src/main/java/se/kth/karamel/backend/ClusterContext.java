/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.launcher.google.GceContext;
import se.kth.karamel.client.model.Ec2;
import se.kth.karamel.client.model.Gce;
import se.kth.karamel.client.model.Provider;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class ClusterContext {

  private Ec2Context ec2Context;
  private GceContext gceContext;
  private SshKeyPair sshKeyPair;
  private String sudoAccountPassword = "";
  private boolean sudoAccountPasswordRequired = false;
  private String githubEmail = "anonymous@anonymous.org";
  private String githubPassword;

  public void setSudoAccountPasswordRequired(boolean sudoAccountPasswordRequired) {
    this.sudoAccountPasswordRequired = sudoAccountPasswordRequired;
  }

  public boolean isSudoAccountPasswordRequired() {
    return sudoAccountPasswordRequired;
  }

  public void setGithubEmail(String githubEmail) {
    this.githubEmail = githubEmail;
  }

  public void setGithubPassword(String githubPassword) {
    this.githubPassword = githubPassword;
  }

  public void setSudoAccountPassword(String sudoAccountPassword) {
    this.sudoAccountPassword = sudoAccountPassword;
  }

  public String getGithubEmail() {
    return githubEmail;
  }

  public String getGithubUsername() {
    return githubEmail.substring(0, githubEmail.lastIndexOf("@"));
  }

  public String getGithubPassword() {
    return githubPassword;
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
    if (gceContext == null) {
      gceContext = commonContext.getGceContext();
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
      if (provider instanceof Ec2 && context.getEc2Context() == null) {
        throw new KaramelException("No valid Ec2 credentials registered :-|");
      } else if (provider instanceof Gce && context.getGceContext() == null) {
        throw new KaramelException("No valid Gce credentials registered :-|");
      }
    }

    if (context.getSshKeyPair() == null) {
      throw new KaramelException("No ssh keypair chosen :-|");
    }
    return context;
  }

  /**
   * @return the gceContext
   */
  public GceContext getGceContext() {
    return gceContext;
  }

  /**
   * @param gceContext the gceContext to set
   */
  public void setGceContext(GceContext gceContext) {
    this.gceContext = gceContext;
  }
}
