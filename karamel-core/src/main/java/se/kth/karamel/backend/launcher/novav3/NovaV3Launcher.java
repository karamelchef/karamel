package se.kth.karamel.backend.launcher.novav3;

import com.google.common.base.Predicate;
/*
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
*/
import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.api.Builders;
import org.openstack4j.api.compute.ComputeSecurityGroupService;
//import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.SecGroupExtension;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.IPProtocol;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.FloatingIP;
//import org.openstack4j.model.common.Identifier;

import org.apache.log4j.Logger;
import org.jclouds.compute.domain.NodeMetadata;
//import org.jclouds.compute.RunNodesException;
import org.jclouds.http.HttpResponseException;

/*
import org.jclouds.ContextBuilder;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.net.domain.IpProtocol;

import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.Ingress;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
*/
//import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.launcher.Launcher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Nova;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.util.NovaCredentials;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.util.settings.NovaSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Alberto on 2015-05-16.
 */
public final class NovaV3Launcher extends Launcher {
  private static final Logger logger = Logger.getLogger(NovaV3Launcher.class);
  private static boolean TESTING = true;
  private final NovaV3Context novaContext;
  private final SshKeyPair sshKeyPair;

  private Set<String> keys = new HashSet<>();

  public NovaV3Launcher(NovaV3Context novaContext, SshKeyPair sshKeyPair) throws KaramelException {
    if (novaContext == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    } else if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    } else {
      this.novaContext = novaContext;
      this.novaContext.reauth();
      this.sshKeyPair = sshKeyPair;
      //logger.info(String.format("Account-Name='%s'", novaContext.getNovaCredentials().getAccountName()));
      //logger.info(String.format("Public-key='%s'", sshKeyPair.getPublicKeyPath()));
      //logger.info(String.format("Private-key='%s'", sshKeyPair.getPrivateKeyPath()));
    }
  }

  public static NovaV3Context validateCredentials(NovaCredentials novaCredentials)
          throws InvalidNovaCredentialsException {
    try {
      NovaV3Context context = new NovaV3Context(novaCredentials);
      context.authenticate();
      //this.novaContext.getCmpute().servers().list();
      return context;
    } catch (AuthenticationException e) {
      throw new InvalidNovaCredentialsException("account-name:" + novaCredentials.getAccountName(), e);
    }
  }

  public static NovaCredentials readCredentials(Confs confs) {
    String accountId = confs.getProperty(NovaSetting.NOVA_ACCOUNT_ID_KEY.getParameter());
    String accessKey = confs.getProperty(NovaSetting.NOVA_ACCESSKEY_KEY.getParameter());
    String endpoint = confs.getProperty(NovaSetting.NOVA_ACCOUNT_ENDPOINT.getParameter());
    String novaRegion = confs.getProperty(NovaSetting.NOVA_REGION.getParameter());
    String novaNetworkId = confs.getProperty(NovaSetting.NOVA_NETWORKID.getParameter());
    NovaCredentials novaCredentials = null;
    if (accountId != null && !accountId.isEmpty() && accessKey != null && !accessKey.isEmpty()
            && endpoint != null && !endpoint.isEmpty() && novaRegion != null && !novaRegion.isEmpty()) {
      novaCredentials = new NovaCredentials();
      novaCredentials.setAccountName(accountId);
      novaCredentials.setAccountPass(accessKey);
      novaCredentials.setEndpoint(endpoint);
      novaCredentials.setRegion(novaRegion);
      novaCredentials.setNetworkId(novaNetworkId);
    }
    return novaCredentials;
  }

  public String createSecurityGroup(String clusterName, String groupName, Nova nova, Set<String> ports) {
    String securityGroupUniqueName = NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName, groupName);
    logger.info(String.format("Creating security group '%s' ...", securityGroupUniqueName));

    SecGroupExtension group = this.novaContext.getCompute().securityGroups().create(securityGroupUniqueName,
        String.format("Security group for hops cluster %s, node group %s", clusterName, groupName));

    //Go over the ips
    if (!TESTING) {
      for (String port : ports) {
        Integer portNumber;
        IPProtocol ipProtocol;
        if (port.contains("/")) {
          String[] s = port.split("/");
          portNumber = Integer.valueOf(s[0]);
          ipProtocol = IPProtocol.valueOf(s[1]);
        } else {
          portNumber = Integer.valueOf(port);
          ipProtocol = IPProtocol.TCP;
        }

        SecGroupExtension.Rule rule = this.novaContext.getCompute().securityGroups()
          .createRule(Builders.secGroupRule()
              .parentGroupId(group.getId())
              .protocol(IPProtocol.TCP)
              .cidr("0.0.0.0/0")
              .range(portNumber, portNumber).build()
              );

        logger.info(String.format("Ports became open for '%s'", securityGroupUniqueName));
      }
    } else {
      SecGroupExtension.Rule rule = this.novaContext.getCompute().securityGroups()
        .createRule(Builders.secGroupRule()
            .parentGroupId(group.getId())
            .protocol(IPProtocol.TCP)
            .cidr("0.0.0.0/0")
            .range(1, 65535).build()
            );

      logger.info(String.format("Ports became open for '%s'", securityGroupUniqueName));
    }
    logger.info(String.format("Security group '%s' was created :)", securityGroupUniqueName));
    return group.getId();
  }

  public boolean uploadSshPublicKey(String keyPairName, Nova nova, boolean removeOld) {

    if (removeOld) {
      ActionResponse res = this.novaContext.getCompute().keypairs().delete(keyPairName);
      if (!res.isSuccess())
        logger.info(String.format("Could not remove key maube it does not exist '%s'", keyPairName));
    }

    logger.info(String.format("New keypair '%s' is being uploaded to Nova OpenStack", keyPairName));
    this.novaContext.getCompute().keypairs().create(keyPairName, sshKeyPair.getPublicKey());
    
    return true;
  }


  public boolean cleanupFailedNodes(Map<NodeMetadata, Throwable> failedNodes) {
    boolean success = false;
    /*/
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
    }*/
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
      logger.info(String.format("Deleteing with provider %s", provider));
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
        // Get right region
        groupRegion.put(group.getName(), "RegionOne");
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
    for (Server s : this.novaContext.getCompute().servers().list()) {
      String gname = s.getMetadata().get("Group");
      if (gname != null && gname != "" && groupNames.contains(gname)) {
        logger.info(String.format("Deleting server with name %s ", s.getName()));
        this.novaContext.getCompute().servers().delete(s.getId());
      }
    }

    logger.info(String.format("All machines destroyed in all the security groups. :) "));
    
    for (String nodeId : vmIds) {
      logger.info(String.format("Deleteing server with id %s", nodeId));
      ActionResponse res = this.novaContext.getCompute().servers().delete(nodeId);
      if (!res.isSuccess()) {
        logger.info(String.format("Could not kill server with id %s", nodeId));
      }
    }


    ComputeSecurityGroupService sec = this.novaContext.getCompute().securityGroups();

    for (Map.Entry<String, String> gp : groupRegion.entrySet()) {
      String uniqueGroupName = NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName, gp.getKey());
      for (SecGroupExtension secgroup : sec.list()) {
        //TODO find the real name of the jclouds groups in openstack
        if (secgroup.getName().startsWith(uniqueGroupName) || secgroup.getName().equals(uniqueGroupName)) {
          logger.info(String.format("Destroying security group '%s' ...", secgroup.getName()));
          boolean retry = false;
          int count = 0;
          do {
            count++;
            try {
              logger.info(String.format("#%d Destroying security group '%s' ...", count, secgroup.getName()));
              sec.delete(secgroup.getId());
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
    
    Set<String> ports = new HashSet<>();
    JsonGroup jg = UserClusterDataExtractor.findGroup(definition,name);
    Provider provider = UserClusterDataExtractor.getGroupProvider(definition,name);
    Nova nova = (Nova) provider;
    
    ports.addAll(Settings.AWS_VM_PORTS_DEFAULT);
    return createSecurityGroup(definition.getName(), jg.getName(), nova, ports);

  }

  @Override
  public List<MachineRuntime> forkMachines(JsonCluster definition, ClusterRuntime runtime, String name)
          throws KaramelException {

    Nova nova = (Nova) UserClusterDataExtractor.getGroupProvider(definition,name);
    JsonGroup definedGroup = UserClusterDataExtractor.findGroup(definition, name);
    GroupRuntime groupRuntime = UserClusterDataExtractor.findGroup(runtime,name);
    Set<String> groupIds = new HashSet<>();
    
    groupIds.add(groupRuntime.getId());
    
    String keypairName = NovaSetting.NOVA_KEYPAIR_NAME(runtime.getName(),
        this.novaContext.getNovaCredentials().getRegion());
    if(!keys.contains(keypairName)) {
      uploadSshPublicKey(keypairName, nova, true);
      keys.add(keypairName);
    }
    return requestNodes(keypairName, groupRuntime, groupIds, Integer.valueOf(definedGroup.getSize()), nova);
  }

  private List<MachineRuntime> requestNodes(String keypairName, GroupRuntime groupRuntime, Set<String> groupIds,
                                            Integer totalSize, Nova nova) throws KaramelException {
    String uniqueGroupName = NovaSetting.NOVA_UNIQUE_GROUP_NAME(groupRuntime.getCluster().getName(),
            groupRuntime.getName());

    List<String> allVmNames = NovaSetting.NOVA_UNIQUE_VM_NAMES(groupRuntime.getCluster().getName(),
            groupRuntime.getName(), totalSize.intValue());

    List<String> leftVmNames = NovaSetting.NOVA_UNIQUE_VM_NAMES(groupRuntime.getCluster().getName(),
            groupRuntime.getName(), totalSize.intValue());

    logger.info(String.format("Start forking %d machine(s) for '%s' ...", totalSize, uniqueGroupName));

    boolean succeed = false;
    int tries = 0;
    List<Server> successfulNodes = new ArrayList<>();
    List<Server> forkedVms = new ArrayList<>();
    Map<NodeMetadata, Throwable> failedNodes = Maps.newHashMap();

    this.novaContext.getOsClient().useRegion(novaContext.getNovaCredentials().getRegion());

    while (!succeed && tries < Settings.AWS_RETRY_MAX) {
      int requestSize = totalSize - successfulNodes.size();
      int maxForkRequests = Integer.parseInt(NovaSetting.NOVA_MAX_FORK_VMS_PER_REQUEST.getParameter());
      if (requestSize > maxForkRequests) {
        requestSize = maxForkRequests;
      } else {
        tries++;
      }
      List<String> networks = Arrays.asList(novaContext.getNovaCredentials().getNetworkId());
       
      try {
        logger.info(String.format("Forking %d machine(s) for '%s', so far(succeeded:%d, failed:%d, total:%d)",
                requestSize, uniqueGroupName, successfulNodes.size(), failedNodes.size(), totalSize));
        for (String nodeName : leftVmNames) {
          logger.info(String.format("Building server with name '%s'", nodeName));
          ServerCreate sc = Builders.server()
            .name(nodeName)
            .flavor(nova.getFlavor())
            .image(nova.getImage())
            .networks(networks)
            .keypairName(keypairName)
            .addMetadataItem("Group", uniqueGroupName)
            .addMetadataItem("sshuser", nova.getUsername())
            .addMetadataItem("sshport", "22")
            .addMetadataItem("Descr", "Created by karamel(v3)")
            .build();

          for (String secGroupId : groupIds) {
            sc.addSecurityGroup(secGroupId);
          }
          Server server = this.novaContext.getCompute().servers().boot(sc);
          forkedVms.add(server);
        }
        /*
        succ.addAll(novaContext.getComputeService().createNodesInGroup(
                uniqueGroupName, requestSize, template.build()));
        */
      } catch (HttpResponseException e) {
        //Need error handling on the different possible
        logger.error("", e);

      } catch (IllegalStateException ex) {
        logger.error("", ex);
        logger.info(String.format("#%d Hurry up Nova!! I want machines for %s, will ask you again in %d ms :@", tries,
                uniqueGroupName, NovaSetting.NOVA_RETRY_INTERVAL), ex);
      }

      successfulNodes = handleForkedNodes(forkedVms, leftVmNames);
      if (successfulNodes.size() < totalSize) {
        try {
          succeed = false;
          logger.info(String.format("So far we got %d successful-machine(s) out of %d", successfulNodes.size(),
                  totalSize, uniqueGroupName));
          Thread.currentThread().sleep(Settings.AWS_RETRY_INTERVAL);
        } catch (InterruptedException ex1) {
          logger.error("", ex1);
        }
      } else {
        succeed = true;
        logger.info(String.format("Cool!! we got all %d machine(s) for '%s' |;-)", totalSize, uniqueGroupName));
        
        List<MachineRuntime> machines = new ArrayList<>();

        for (Server s : successfulNodes) {

          FloatingIP server_extip = this.getFloatingIp();

          if (server_extip == null) {
            // TODO: delete server !?
            logger.info(String.format("Failed to alloc flaoting ip :("));
            continue;
          }

          ActionResponse r = this.novaContext.getCompute().floatingIps()
            .addFloatingIP(s, server_extip.getFloatingIpAddress());
          if (!r.isSuccess()) {
            logger.info(String.format("Failed to addFloatinIp to %s", s.getName()));
            continue;
          }

          String privateIp = null;
          String floatingIp = server_extip.getFloatingIpAddress();
          
          // Get the first addresses
          Map<String, List<? extends Address>> adrMap = s.getAddresses().getAddresses();
          for (String key : adrMap.keySet()) {
            List<? extends Address> adrList = adrMap.get(key);
            for (Address adr : adrList) {
              logger.info(String.format("Network resource key: {} of instance: {}, address: {}",
                    key, s.getName(), adr.getAddr()));
              switch (adr.getType()) {
                case "fixed":
                  if (privateIp == null)
                    privateIp = adr.getAddr();
                  break;
                case "floating":
                  if (floatingIp == null)
                    floatingIp = adr.getAddr();
                  break;
                default:
                  logger.error(String.format("No such network resource type: {}, instance: {}",
                        adr.getType(), s.getName()));
              }
            }
          }

          // Set data about server  
          MachineRuntime machine = new MachineRuntime(groupRuntime);
          machine.setVmId(s.getId());
          machine.setName(s.getName());
          machine.setPrivateIp(privateIp);

          machine.setPublicIp(floatingIp);

          machine.setSshPort(Integer.valueOf(s.getMetadata().get("sshport")));
          machine.setSshUser(s.getMetadata().get("sshuser"));
          machines.add(machine);
        }
        return machines;
      }
    }
    throw new KaramelException(String.format("Couldn't fork machines for group'%s'", groupRuntime.getName()));
  }

  private FloatingIP getFloatingIp() {

    // Check for free ones
    for (FloatingIP ip : this.novaContext.getCompute().floatingIps().list()) {
      logger.info(String.format("Ip addresses %s", ip.getFixedIpAddress()));
      if (ip.getFixedIpAddress() == null || ip.getFixedIpAddress().equals("")) {
        return ip;
      }
    }

    // Try to allocate 
    for (String poolName : this.novaContext.getCompute().floatingIps().getPoolNames()) {
      FloatingIP ip = null;
      try { 
        ip = this.novaContext.getCompute().floatingIps().allocateIP(poolName);
      } catch (ClientResponseException e) {
        logger.info(String.format("Could not allocate floating ip, status code %s: ", e.getStatusCode()), e);
        ip = null;
        continue;
      }
      return ip;
    }

    return null;
  }

  /*
  private getAddresses(Server server) {
    return server.getAddresses().getAddresses();
  }
  */

  private List<Server> handleForkedNodes(List<Server> forkedNodes, List<String> leftNodes) {
    List<Server> activeNodes = new ArrayList<>();
    List<Server> failedNodes = new ArrayList<>();
    
    int unnamedVms = 0;
    for (Server s : forkedNodes ) {
      logger.info(String.format("Checking node %s", s.getId()));
      Server updated_s = this.novaContext.getCompute().servers().get(s.getId());
      String nodeName = updated_s.getName();
      leftNodes.remove(nodeName);
      if (updated_s.getStatus() == Server.Status.ACTIVE) {
        activeNodes.add(updated_s);
      } else if (updated_s.getCreated() != null &&
          updated_s.getCreated().getTime() + (10*60*1000) < new Date().getTime()) {
        logger.info(String.format("Server %s(%s) createdi at %s now is %s did not start in time",
              nodeName, s.getId(), updated_s.getCreated().getTime(), new Date().getTime()));
        logger.info(String.format("Server %s(%s) did not start in time lets try again", nodeName, s.getId()));
        this.novaContext.getCompute().servers().delete(s.getId());
        // Readd so we restart it
        leftNodes.add(nodeName);
        failedNodes.add(s);
      }
    }

    // removed failed
    forkedNodes.removeAll(failedNodes);

    return activeNodes;
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
