/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.SshKeyPair;

/**
 * Authenticated APIs and privacy-sensitive data, that must not be revealed by storing them in the file-system, is
 * stored here in memory. It is valid just until the system is running otherwise it will disappear.
 *
 *
 * @author kamal
 */
public class ClusterContext {

  private SshKeyPair sshKeyPair;
  private String sudoAccountPassword = "";

  public void setSudoAccountPassword(String sudoAccountPassword) {
    this.sudoAccountPassword = sudoAccountPassword;
  }

  public String getSudoAccountPassword() {
    return sudoAccountPassword;
  }

  public SshKeyPair getSshKeyPair() {
    return sshKeyPair;
  }

  public void setSshKeyPair(SshKeyPair sshKeyPair) {
    this.sshKeyPair = sshKeyPair;
  }

  public void mergeContext(ClusterContext commonContext) {
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

    if (context.getSshKeyPair() == null) {
      throw new KaramelException("No ssh keypair chosen :-|");
    }
    return context;
  }

}
