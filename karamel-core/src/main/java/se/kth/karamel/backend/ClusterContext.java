/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.github.GithubApi;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.launcher.google.GceContext;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.client.model.Nova;
/**
 * Authenticated APIs and privacy-sensitive data, that must not be revealed by storing them in the file-system, is
 * stored here in memory. It is valid just until the system is running otherwise it will disappear.  *
 * @author kamal
 */
public class ClusterContext {

  private Ec2Context ec2Context;
  private GceContext gceContext;
  private SshKeyPair sshKeyPair;
  private String sudoAccountPassword = "";

  public void setSudoAccountPassword(String sudoAccountPassword) {
    this.sudoAccountPassword = sudoAccountPassword;
  }

  public String getGithubUsername() {
    return GithubApi.getEmail().isEmpty() ? "karamel" : GithubApi.getEmail().substring(0,
        GithubApi.getEmail().lastIndexOf("@"));
  }

  public String getSudoAccountPassword() {
    return sudoAccountPassword;
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
    if (novaContext == null){
      novaContext = commonContext.getNovaContext();
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
      } else if (provider instanceof Gce && context.getGceContext() == null) {
        throw new KaramelException("No valid Gce credentials registered :-|");
      } else if (provider instanceof Nova && context.getNovaContext() == null){
        throw new KaramelException("No valid Nova credentials registered :-|");
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

  public void setNovaContext(NovaContext novaContext) {
    this.novaContext = novaContext;
  }
}
