package se.kth.karamel.backend.launcher.nova;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.openstack.nova.v2_0.domain.Ingress;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.backend.launcher.Launcher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.client.model.Nova;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.NovaCredentials;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.settings.NovaSetting;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Alberto on 2015-05-16.
 */
public final class NovaLauncher extends Launcher{
  private static final Logger logger = Logger.getLogger(NovaLauncher.class);
  public static boolean TESTING = true;
  public final NovaContext novaContext;
  public final SshKeyPair sshKeyPair;

  public NovaLauncher(NovaContext novaContext, SshKeyPair sshKeyPair) throws KaramelException {
    if (novaContext == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    } else if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    } else {
      this.novaContext = novaContext;
      this.sshKeyPair = sshKeyPair;
      logger.info(String.format("Account-Name='%s'", novaContext.getNovaCredentials().getAccountName()));
      logger.info(String.format("Public-key='%s'", sshKeyPair.getPublicKeyPath()));
      logger.info(String.format("Private-key='%s'", sshKeyPair.getPrivateKeyPath()));
    }
  }

  public static NovaContext validateCredentials(NovaCredentials novaCredentials, ContextBuilder builder)
          throws InvalidNovaCredentialsException {
    try {
      NovaContext context = new NovaContext(novaCredentials, builder);
      SecurityGroupApi securityGroupApi = context.getSecurityGroupApi();
      securityGroupApi.list();
      return context;
    } catch (AuthorizationException e) {
      throw new InvalidNovaCredentialsException("account-name:" + novaCredentials.getAccountName(), e);
    }
  }

  public static NovaCredentials readCredentials(Confs confs) {
    String accountId = confs.getProperty(NovaSetting.NOVA_ACCOUNT_ID_KEY.getParameter());
    String accessKey = confs.getProperty(NovaSetting.NOVA_ACCESSKEY_KEY.getParameter());
    String endpoint = confs.getProperty(NovaSetting.NOVA_ACCOUNT_ENDPOINT.getParameter());
    String novaRegion = confs.getProperty(NovaSetting.NOVA_REGION.getParameter());
    NovaCredentials novaCredentials = null;
    if (accountId != null && !accountId.isEmpty() && accessKey != null && !accessKey.isEmpty()
            && endpoint != null && !endpoint.isEmpty() && novaRegion != null && !novaRegion.isEmpty()) {
      novaCredentials = new NovaCredentials();
      novaCredentials.setAccountName(accountId);
      novaCredentials.setAccountPass(accessKey);
      novaCredentials.setEndpoint(endpoint);
      novaCredentials.setRegion(novaRegion);
    }
    return novaCredentials;
  }

  public String createSecurityGroup(String clusterName, String groupName, Nova nova, Set<String> ports) {
    String securityGroupUniqueName = NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName, groupName);
    logger.info(String.format("Creating security group '%s' ...", securityGroupUniqueName));
    Optional<? extends SecurityGroupApi> securityGroupExt = novaContext.getNovaApi().getSecurityGroupApi(nova
            .getRegion());
    if (securityGroupExt.isPresent()) {
      SecurityGroupApi client = securityGroupExt.get();

      String groupId;
      //TODO Do we have something similar to VPC EC2 in Nova?
      SecurityGroup created = client.createWithDescription(securityGroupUniqueName, NovaSetting
              .NOVA_UNIQUE_GROUP_DESCRIPTION(clusterName, groupName));
      //Get id of the security group
      groupId = created.getId();
      //Go over the ips
      if (!TESTING) {
        for (String port : ports) {
          Integer portNumber;
          IpProtocol ipProtocol;
          if (port.contains("/")) {
            String[] s = port.split("/");
            portNumber = Integer.valueOf(s[0]);
            ipProtocol = IpProtocol.valueOf(s[1]);
          } else {
            portNumber = Integer.valueOf(port);
            ipProtocol = IpProtocol.TCP;
          }
          Ingress ingress = Ingress.builder()
                  .fromPort(portNumber)
                  .toPort(portNumber)
                  .ipProtocol(ipProtocol)
                  .build();

          client.createRuleAllowingCidrBlock(groupId, ingress, "0.0.0.0/0");
          logger.info(String.format("Ports became open for '%s'", securityGroupUniqueName));
        }
      } else {
        Ingress ingress = Ingress.builder()
                .fromPort(0)
                .toPort(65535)
                .ipProtocol(IpProtocol.TCP)
                .build();
        client.createRuleAllowingCidrBlock(groupId, ingress, "0.0.0.0/0");
        logger.info(String.format("Ports became open for '%s'", securityGroupUniqueName));
      }
      logger.info(String.format("Security group '%s' was created :)", securityGroupUniqueName));
      return groupId;
    }
    return null;
  }

  public boolean uploadSshPublicKey(String keyPairName, Nova nova, boolean removeOld) {
    boolean uploadSuccesful;
    FluentIterable<KeyPair> keyPairs = novaContext.getKeyPairApi().list();
    if (keyPairs.isEmpty()) {
      logger.info(String.format("New keypair '%s' is being uploaded to Nova OpenStack", keyPairName));
      novaContext.getKeyPairApi().createWithPublicKey(keyPairName, sshKeyPair.getPublicKey());
      uploadSuccesful = true;
    } else if (removeOld) {
      logger.info(String.format("Removing the old keypair '%s' and uploading the new one ...", keyPairName));
      boolean deleteSuccesful = novaContext.getKeyPairApi().delete(keyPairName);
      KeyPair pair = novaContext.getKeyPairApi().createWithPublicKey(keyPairName, sshKeyPair.getPublicKey());
      uploadSuccesful = deleteSuccesful && pair != null;
    } else {
      uploadSuccesful = false;
    }
    return uploadSuccesful;
  }


  public boolean cleanupFailedNodes(Map<NodeMetadata, Throwable> failedNodes) {
    boolean success;
    if (failedNodes.size() > 0) {
      Set<String> lostIds = Sets.newLinkedHashSet();
      for (Map.Entry<NodeMetadata, Throwable> lostNode : failedNodes.entrySet()) {
        lostIds.add(lostNode.getKey().getId());
      }
      int numberOfNodesToDelete = lostIds.size();
      logger.info(String.format("Destroying failed nodes with ids: %s", lostIds.toString()));
      Set<? extends NodeMetadata> destroyedNodes = novaContext.getComputeService().destroyNodesMatching(
              Predicates.in(failedNodes.keySet()));
      lostIds.clear();
      for (NodeMetadata destroyed : destroyedNodes) {
        lostIds.add(destroyed.getId());
      }
      logger.info("Failed nodes destroyed ;)");
      int numberOfNodesSuccesfullyDeleted = lostIds.size();
      success = numberOfNodesSuccesfullyDeleted == numberOfNodesToDelete;
    } else {
      success = true;
    }
    return success;

  }

  @Override
  public void cleanup(JsonCluster definition, ClusterRuntime runtime) throws KaramelException {

  }

  @Override
  public String forkGroup(JsonCluster definition, ClusterRuntime runtime, String name) throws KaramelException {
    return null;
  }

  @Override
  public List<MachineRuntime> forkMachines(JsonCluster definition, ClusterRuntime runtime, String name) throws KaramelException {
    return null;
  }
}
