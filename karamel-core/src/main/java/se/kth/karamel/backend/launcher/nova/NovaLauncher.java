package se.kth.karamel.backend.launcher.nova;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.http.HttpResponseException;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.Ingress;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.launcher.Launcher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.client.model.Nova;
import se.kth.karamel.client.model.Provider;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.NovaCredentials;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.settings.NovaSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by Alberto on 2015-05-16.
 */
public final class NovaLauncher extends Launcher{
  private static final Logger logger = Logger.getLogger(NovaLauncher.class);
  private static boolean TESTING = true;
  private final NovaContext novaContext;
  private final SshKeyPair sshKeyPair;

  private Set<String> keys = new HashSet<>();

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
    runtime.resolveFailures();
    List<GroupRuntime> groups = runtime.getGroups();
    Set<String> allNovaVms = new HashSet<>();
    Set<String> allNovaVmsIds = new HashSet<>();
    Map<String, String> groupRegion = new HashMap<>();
    for (GroupRuntime group : groups) {
      group.getCluster().resolveFailures();
      Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      if (provider instanceof Nova) {
        for (MachineRuntime machine : group.getMachines()) {
          if (machine.getVmId() != null) {
            allNovaVmsIds.add(machine.getVmId());
          }
        }
        JsonGroup jg = UserClusterDataExtractor.findGroup(definition, group.getName());
        List<String> vmNames = NovaSetting.NOVA_UNIQUE_VM_NAMES(group.getCluster().getName(), group.getName(),
                jg.getSize());
        allNovaVms.addAll(vmNames);
        groupRegion.put(group.getName(), ((Nova) provider).getRegion());
      }
    }
    cleanup(definition.getName(), allNovaVmsIds, allNovaVms, groupRegion);
  }

  public void cleanup(String clusterName, Set<String> vmIds, Set<String> vmNames, Map<String, String> groupRegion)
          throws KaramelException {
    Set<String> groupNames = new HashSet<>();
    for (Map.Entry<String, String> gp : groupRegion.entrySet()) {
      groupNames.add(NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName, gp.getKey()));
    }
    logger.info(String.format("Killing following machines with names: \n %s \nor inside group names %s \nor with ids: "
            + "%s", vmNames.toString(), groupNames, vmIds));
    logger.info(String.format("Killing all machines in groups: %s", groupNames.toString()));
    novaContext.getComputeService().destroyNodesMatching(withPredicate(vmIds, vmNames, groupNames));
    logger.info(String.format("All machines destroyed in all the security groups. :) "));
    for (Map.Entry<String, String> gp : groupRegion.entrySet()) {
      String uniqueGroupName = NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName, gp.getKey());
      for (SecurityGroup secgroup : novaContext.getSecurityGroupApi().list()) {
        //TODO find the real name of the jclouds groups in openstack
        if (secgroup.getName().startsWith("jclouds#" + uniqueGroupName) || secgroup.getName().equals(uniqueGroupName)) {
          logger.info(String.format("Destroying security group '%s' ...", secgroup.getName()));
          boolean retry = false;
          int count = 0;
          do {
            count++;
            try {
              logger.info(String.format("#%d Destroying security group '%s' ...", count, secgroup.getName()));
              novaContext.getSecurityGroupApi().delete(secgroup.getId());
            } catch (IllegalStateException ex) {
              logger.info(String.format("Hurry up Nova!! terminate machines!! '%s', will retry in %d ms :@",
                          uniqueGroupName, NovaSetting.NOVA_RETRY_INTERVAL.getParameter()));
              retry = true;
              try {
                Thread.currentThread().sleep(Long.parseLong(NovaSetting.NOVA_RETRY_INTERVAL.getParameter()));
              } catch (InterruptedException ex1) {
                logger.error("", ex1);
              }
            }
          } while (retry);
          logger.info(String.format("The security group '%s' destroyed ^-^", secgroup.getName()));
        }
      }
    }
  }

  @Override
  public String forkGroup(JsonCluster definition, ClusterRuntime runtime, String name) throws KaramelException {
    JsonGroup jg = UserClusterDataExtractor.findGroup(definition,name);
    Provider provider = UserClusterDataExtractor.getGroupProvider(definition,name);
    Nova nova = (Nova) provider;
    Set<String> ports = new HashSet<>();
    ports.addAll(Settings.EC2_DEFAULT_PORTS);
    String groupId = createSecurityGroup(definition.getName(), jg.getName(), nova, ports);
    return groupId;
  }

  @Override
  public List<MachineRuntime> forkMachines(JsonCluster definition, ClusterRuntime runtime, String name)
          throws KaramelException {
    Nova nova = (Nova) UserClusterDataExtractor.getGroupProvider(definition,name);
    JsonGroup definedGroup = UserClusterDataExtractor.findGroup(definition, name);
    GroupRuntime groupRuntime = UserClusterDataExtractor.findGroup(runtime,name);
    Set<String> groupIds = new HashSet<>();
    groupIds.add(groupRuntime.getId());

    String keypairName = NovaSetting.NOVA_KEYPAIR_NAME(runtime.getName(), nova.getRegion());
    if(!keys.contains(keypairName)){
      uploadSshPublicKey(keypairName,nova,true);
      keys.add(keypairName);
    }
    return requestNodes(keypairName,groupRuntime,groupIds,Integer.valueOf(definedGroup.getSize()),nova);
  }

  private List<MachineRuntime> requestNodes(String keypairName, GroupRuntime groupRuntime, Set<String> groupIds,
                                            Integer totalSize, Nova nova) throws KaramelException {
    String uniqueGroupName = NovaSetting.NOVA_UNIQUE_GROUP_NAME(groupRuntime.getCluster().getName(),
            groupRuntime.getName());

    List<String> allVmNames = NovaSetting.NOVA_UNIQUE_VM_NAMES(groupRuntime.getCluster().getName(),
            groupRuntime.getName(), totalSize.intValue());

    logger.info(String.format("Start forking %d machine(s) for '%s' ...", totalSize, uniqueGroupName));

    NovaTemplateOptions options = novaContext.getComputeService().templateOptions();

    boolean succeed = false;
    int tries = 0;
    Set<NodeMetadata> successfulNodes = Sets.newLinkedHashSet();
    List<String> unforkedVmNames = new ArrayList<>();
    List<String> toBeForkedVmNames;
    unforkedVmNames.addAll(allVmNames);
    Map<NodeMetadata, Throwable> failedNodes = Maps.newHashMap();
    while (!succeed && tries < Settings.EC2_RETRY_MAX) {
      int requestSize = totalSize - successfulNodes.size();
      int maxForkRequests = Integer.parseInt(NovaSetting.NOVA_MAX_FORK_VMS_PER_REQUEST.getParameter());
      if (requestSize > maxForkRequests) {
        requestSize = maxForkRequests;
        toBeForkedVmNames = unforkedVmNames.subList(0, maxForkRequests);
      } else {
        toBeForkedVmNames = unforkedVmNames;
      }
      TemplateBuilder template = novaContext.getComputeService().templateBuilder();
      options.keyPairName(keypairName);
      options.securityGroups(groupIds);
      options.nodeNames(toBeForkedVmNames);

      template.options(options);
      template.os64Bit(true);
      template.hardwareId(nova.getFlavor());
      template.imageId(nova.getImage());
      template.locationId(nova.getRegion());
      tries++;
      Set<NodeMetadata> succ = new HashSet<>();
      try {
        logger.info(String.format("Forking %d machine(s) for '%s', so far(succeeded:%d, failed:%d, total:%d)",
                requestSize, uniqueGroupName, successfulNodes.size(), failedNodes.size(), totalSize));
        succ.addAll(novaContext.getComputeService().createNodesInGroup(
                uniqueGroupName, requestSize, template.build()));
      } catch (RunNodesException ex) {
        addSuccessAndLostNodes(ex, succ, failedNodes);
      } catch (HttpResponseException e) {
        //Need error handling on the different possible
        logger.error("", e);

      } catch (IllegalStateException ex) {
        logger.error("", ex);
        logger.info(String.format("#%d Hurry up Nova!! I want machines for %s, will ask you again in %d ms :@", tries,
                uniqueGroupName, NovaSetting.NOVA_RETRY_INTERVAL), ex);
      }

      unforkedVmNames = findLeftVmNames(succ, unforkedVmNames);
      successfulNodes.addAll(succ);
      if (successfulNodes.size() < totalSize) {
        try {
          succeed = false;
          logger.info(String.format("So far we got %d successful-machine(s) and %d failed-machine(s) out of %d "
                          + "original-number for '%s'. Failed nodes will be killed later.", successfulNodes.size(),
                  failedNodes.size(),
                  totalSize, uniqueGroupName));
          Thread.currentThread().sleep(Settings.EC2_RETRY_INTERVAL);
        } catch (InterruptedException ex1) {
          logger.error("", ex1);
        }
      } else {
        succeed = true;
        logger.info(String.format("Cool!! we got all %d machine(s) for '%s' |;-) we have %d failed-machines to kill "
                + "before we go on..", totalSize, uniqueGroupName, failedNodes.size()));
        if (failedNodes.size() > 0) {
          cleanupFailedNodes(failedNodes);
        }
        List<MachineRuntime> machines = new ArrayList<>();
        for (NodeMetadata node : successfulNodes) {
          if (node != null) {
            MachineRuntime machine = new MachineRuntime(groupRuntime);
            ArrayList<String> privateIps = new ArrayList();
            ArrayList<String> publicIps = new ArrayList();
            privateIps.addAll(node.getPrivateAddresses());
            publicIps.addAll(node.getPublicAddresses());
            machine.setVmId(node.getId());
            machine.setName(node.getName());
            machine.setPrivateIp(privateIps.get(0));
            machine.setPublicIp(publicIps.get(0));
            machine.setSshPort(node.getLoginPort());
            machine.setSshUser(node.getCredentials().getUser());
            machines.add(machine);
          }
        }
        return machines;
      }
    }
    throw new KaramelException(String.format("Couldn't fork machines for group'%s'", groupRuntime.getName()));
  }

  private void addSuccessAndLostNodes(RunNodesException rnex, Set<NodeMetadata> successfulNodes, Map<NodeMetadata,
          Throwable> lostNodes) {
    // workaround https://code.google.com/p/jclouds/issues/detail?id=923
    // by ensuring that any nodes in the "NodeErrors" do not get considered
    // successful
    Set<? extends NodeMetadata> reportedSuccessfulNodes = rnex.getSuccessfulNodes();
    Map<? extends NodeMetadata, ? extends Throwable> errorNodesMap = rnex.getNodeErrors();
    Set<? extends NodeMetadata> errorNodes = errorNodesMap.keySet();

    // "actual" successful nodes are ones that don't appear in the errorNodes
    successfulNodes.addAll(Sets.difference(reportedSuccessfulNodes, errorNodes));
    lostNodes.putAll(errorNodesMap);
  }

  private List<String> findLeftVmNames(Set<? extends NodeMetadata> successfulNodes, List<String> vmNames) {
    List<String> leftVmNames = new ArrayList<>();
    leftVmNames.addAll(vmNames);
    int unnamedVms = 0;
    for (NodeMetadata nodeMetadata : successfulNodes) {
      String nodeName = nodeMetadata.getName();
      if (leftVmNames.contains(nodeName)) {
        leftVmNames.remove(nodeName);
      } else {
        unnamedVms++;
      }
    }

    for (int i = 0; i < unnamedVms; i++) {
      if (leftVmNames.size() > 0) {
        logger.debug(String.format("Taking %s as one of the unnamed vms.", leftVmNames.get(0)));
        leftVmNames.remove(0);
      }
    }
    return leftVmNames;
  }

  public static Predicate<NodeMetadata> withPredicate(final Set<String> ids, final Set<String> names,
                                                      final Set<String> groupNames) {
    return new Predicate<NodeMetadata>() {
      @Override
      public boolean apply(NodeMetadata nodeMetadata) {
        String id = nodeMetadata.getId();
        String name = nodeMetadata.getName();
        String group = nodeMetadata.getGroup();
        return ((id != null && ids.contains(id)) || (name != null && names.contains(name) ||
                (group != null && groupNames.contains(group))));
      }

      @Override
      public String toString() {
        return "machines predicate";
      }
    };
  }
}
