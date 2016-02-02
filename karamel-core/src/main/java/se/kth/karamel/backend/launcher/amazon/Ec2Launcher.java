/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.launcher.amazon;

import se.kth.karamel.common.launcher.amazon.InstanceType;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jclouds.aws.AWSResponseException;
import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.aws.ec2.features.AWSSecurityGroupApi;
import org.jclouds.aws.ec2.options.CreateSecurityGroupOptions;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.ec2.domain.BlockDeviceMapping;
import org.jclouds.ec2.domain.KeyPair;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.ec2.features.SecurityGroupApi;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.launcher.Launcher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.exception.InvalidEc2CredentialsException;

/**
 * @author kamal
 */
public final class Ec2Launcher extends Launcher {

  private static final Logger logger = Logger.getLogger(Ec2Launcher.class);
  public static boolean TESTING = true;
  public final Ec2Context context;
  public final SshKeyPair sshKeyPair;

  Set<String> keys = new HashSet<>();

  public Ec2Launcher(Ec2Context context, SshKeyPair sshKeyPair) {
    this.context = context;
    this.sshKeyPair = sshKeyPair;
    logger.info(String.format("Access-key='%s'", context.getCredentials().getAccessKey()));
    logger.info(String.format("Public-key='%s'", sshKeyPair.getPublicKeyPath()));
    logger.info(String.format("Private-key='%s'", sshKeyPair.getPrivateKeyPath()));
  }

  public static Ec2Context validateCredentials(Ec2Credentials credentials) throws InvalidEc2CredentialsException {
    try {
      if (credentials.getAccessKey().isEmpty() || credentials.getSecretKey().isEmpty()) {
        throw new InvalidEc2CredentialsException("Ec2 credentials empty - not entered yet.");
      }
      Ec2Context cxt = new Ec2Context(credentials);
      SecurityGroupApi securityGroupApi = cxt.getSecurityGroupApi();
      securityGroupApi.describeSecurityGroupsInRegion(Settings.AWS_REGION_CODE_DEFAULT);
      return cxt;
    } catch (AuthorizationException e) {
      throw new InvalidEc2CredentialsException(e.getMessage() + " - accountid:" + credentials.getAccessKey(), e);
    }
  }

  public static Ec2Credentials readCredentials(Confs confs) {
    String accessKey = System.getenv(Settings.AWS_ACCESSKEY_ENV_VAR);
    String secretKey = System.getenv(Settings.AWS_SECRETKEY_ENV_VAR);

    if (accessKey == null || accessKey.isEmpty()) {
      accessKey = confs.getProperty(Settings.AWS_ACCESSKEY_KEY);
    }
    if (secretKey == null || secretKey.isEmpty()) {
      secretKey = confs.getProperty(Settings.AWS_SECRETKEY_KEY);
    }
    Ec2Credentials credentials = null;
    if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !accessKey.isEmpty()) {
      credentials = new Ec2Credentials();
      credentials.setAccessKey(accessKey);
      credentials.setSecretKey(secretKey);
    }
    return credentials;
  }

  @Override
  public String forkGroup(JsonCluster definition, ClusterRuntime runtime, String groupName) throws KaramelException {
    JsonGroup jg = UserClusterDataExtractor.findGroup(definition, groupName);
    Provider provider = UserClusterDataExtractor.getGroupProvider(definition, groupName);
    Ec2 ec2 = (Ec2) provider;
    Set<String> ports = new HashSet<>();
    ports.addAll(Settings.AWS_VM_PORTS_DEFAULT);
    String groupId = createSecurityGroup(definition.getName(), jg.getName(), ec2, ports);
    return groupId;
  }

  public String createSecurityGroup(String clusterName, String groupName, Ec2 ec2, Set<String> ports)
      throws KaramelException {
    String uniqeGroupName = Settings.AWS_UNIQUE_GROUP_NAME(clusterName, groupName);
    logger.info(String.format("Creating security group '%s' ...", uniqeGroupName));
    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    }

    Optional<? extends org.jclouds.ec2.features.SecurityGroupApi> securityGroupExt
        = context.getEc2api().getSecurityGroupApiForRegion(ec2.getRegion());
    if (securityGroupExt.isPresent()) {
      AWSSecurityGroupApi client = (AWSSecurityGroupApi) securityGroupExt.get();
      String groupId = null;
      if (ec2.getVpc() != null) {
        CreateSecurityGroupOptions csgos = CreateSecurityGroupOptions.Builder.vpcId(ec2.getVpc());
        groupId = client.createSecurityGroupInRegionAndReturnId(ec2.getRegion(), uniqeGroupName, uniqeGroupName, csgos);
      } else {
        groupId = client.createSecurityGroupInRegionAndReturnId(ec2.getRegion(), uniqeGroupName, uniqeGroupName);
      }

      if (!TESTING) {
        for (String port : ports) {
          Integer p = null;
          IpProtocol pr = null;
          if (port.contains("/")) {
            String[] s = port.split("/");
            p = Integer.valueOf(s[0]);
            pr = IpProtocol.valueOf(s[1]);
          } else {
            p = Integer.valueOf(port);
            pr = IpProtocol.TCP;
          }
          client.authorizeSecurityGroupIngressInRegion(ec2.getRegion(),
              uniqeGroupName, pr, p, Integer.valueOf(port), "0.0.0.0/0");
          logger.info(String.format("Ports became open for '%s'", uniqeGroupName));
        }
      } else {
        IpPermission tcpPerms = IpPermission.builder().ipProtocol(IpProtocol.TCP).
            fromPort(0).toPort(65535).cidrBlock("0.0.0.0/0").build();
        IpPermission udpPerms = IpPermission.builder().ipProtocol(IpProtocol.UDP).
            fromPort(0).toPort(65535).cidrBlock("0.0.0.0/0").build();
        ArrayList<IpPermission> perms = Lists.newArrayList(tcpPerms, udpPerms);
        client.authorizeSecurityGroupIngressInRegion(ec2.getRegion(), groupId, perms);
        logger.info(String.format("Ports became open for '%s'", uniqeGroupName));
      }
      logger.info(String.format("Security group '%s' was created :)", uniqeGroupName));
      return groupId;
    }
    return null;
  }

  public void uploadSshPublicKey(String keyPairName, Ec2 ec2, boolean removeOld) throws KaramelException {
    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    }

    HashSet<String> regions = new HashSet();
    if (!regions.contains(ec2.getRegion())) {
      Set<KeyPair> keypairs = context.getKeypairApi().describeKeyPairsInRegion(ec2.getRegion(),
          new String[]{keyPairName});
      if (keypairs.isEmpty()) {
        logger.info(String.format("New keypair '%s' is being uploaded to EC2", keyPairName));
        context.getKeypairApi().importKeyPairInRegion(ec2.getRegion(), keyPairName, sshKeyPair.getPublicKey());
      } else {
        if (removeOld) {
          logger.info(String.format("Removing the old keypair '%s' and uploading the new one ...", keyPairName));
          context.getKeypairApi().deleteKeyPairInRegion(ec2.getRegion(), keyPairName);
          context.getKeypairApi().importKeyPairInRegion(ec2.getRegion(), keyPairName, sshKeyPair.getPublicKey());
        }
      }
      regions.add(ec2.getRegion());
    }
  }

  @Override
  public List<MachineRuntime> forkMachines(JsonCluster definition, ClusterRuntime runtime, String groupName)
      throws KaramelException {
    Ec2 ec2 = (Ec2) UserClusterDataExtractor.getGroupProvider(definition, groupName);
    JsonGroup definedGroup = UserClusterDataExtractor.findGroup(definition, groupName);
    GroupRuntime group = UserClusterDataExtractor.findGroup(runtime, groupName);
    HashSet<String> gids = new HashSet<>();
    gids.add(group.getId());

    String keypairname = Settings.AWS_KEYPAIR_NAME(runtime.getName(), ec2.getRegion());
    if (!keys.contains(keypairname)) {
      uploadSshPublicKey(keypairname, ec2, true);
      keys.add(keypairname);
    }

    int numForked = 0;
    final int numMachines = definedGroup.getSize();
    List<MachineRuntime> allMachines = new ArrayList<>();
    int requestSize = context.getVmBatchSize();
    try {
      while (numForked < numMachines) {
        int forkSize = Math.min(numMachines, requestSize);
        List<MachineRuntime> machines = forkMachines(keypairname, group, gids, numForked, forkSize, ec2);
        allMachines.addAll(machines);
        numForked += forkSize;
      }
    } catch (KaramelException ex) {
      logger.error(
          "Didn't get all machines in this node group. Got " + allMachines.size() + "/" + definedGroup.getSize());
    }
    return allMachines;
  }

  public List<MachineRuntime> forkMachines(String keyPairName, GroupRuntime mainGroup,
      Set<String> securityGroupIds, int startCount, int numberToLaunch, Ec2 ec2) throws KaramelException {
    String uniqueGroupName = Settings.AWS_UNIQUE_GROUP_NAME(mainGroup.getCluster().getName(), mainGroup.getName());
    List<String> allVmNames = Settings.AWS_UNIQUE_VM_NAMES(mainGroup.getCluster().getName(), mainGroup.getName(),
        startCount, numberToLaunch);
    logger.info(String.format("Start forking %d machine(s) for '%s' ...", numberToLaunch, uniqueGroupName));

    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    }
    AWSEC2TemplateOptions options = context.getComputeService().templateOptions().as(AWSEC2TemplateOptions.class);
    if (ec2.getPrice() != null) {
      options.spotPrice(ec2.getPrice());
    }

    Confs confs = Confs.loadKaramelConfs();
    String prepStorages = confs.getProperty(Settings.PREPARE_STORAGES_KEY);
    if (prepStorages != null && prepStorages.equalsIgnoreCase("true")) {
      InstanceType instanceType = InstanceType.valueByModel(ec2.getType());
      List<BlockDeviceMapping> maps = instanceType.getEphemeralDeviceMappings();
      options.blockDeviceMappings(maps);
    }

    boolean succeed = false;
    int tries = 0;
    Set<NodeMetadata> successfulNodes = Sets.newLinkedHashSet();
    List<String> unforkedVmNames = new ArrayList<>();
    List<String> toBeForkedVmNames;
    unforkedVmNames.addAll(allVmNames);
    Map<NodeMetadata, Throwable> failedNodes = Maps.newHashMap();

    int numSuccess = 0, numFailed = 0;

    while (!succeed && tries < Settings.AWS_RETRY_MAX) {
      long startTime = System.currentTimeMillis();
      int requestSize = numberToLaunch - successfulNodes.size();
      if (requestSize > Settings.EC2_MAX_FORK_VMS_PER_REQUEST) {
        requestSize = Settings.EC2_MAX_FORK_VMS_PER_REQUEST;
        toBeForkedVmNames = unforkedVmNames.subList(0, Settings.EC2_MAX_FORK_VMS_PER_REQUEST);
      } else {
        toBeForkedVmNames = unforkedVmNames;
      }
      TemplateBuilder template = context.getComputeService().templateBuilder();
      options.keyPair(keyPairName);
      options.as(AWSEC2TemplateOptions.class).securityGroupIds(securityGroupIds);
      options.nodeNames(toBeForkedVmNames);
      if (ec2.getSubnet() != null) {
        options.as(AWSEC2TemplateOptions.class).subnetId(ec2.getSubnet());
      }
      template.options(options);
      template.os64Bit(true);
      template.hardwareId(ec2.getType());
      template.imageId(ec2.getRegion() + "/" + ec2.getAmi());
      template.locationId(ec2.getRegion());
      tries++;
      Set<NodeMetadata> succ = new HashSet<>();
      try {
        logger.info(String.format("Forking %d machine(s) for '%s', so far(succeeded:%d, failed:%d, total:%d)",
            requestSize, uniqueGroupName, successfulNodes.size(), failedNodes.size(), numberToLaunch));
        succ.addAll(context.getComputeService().createNodesInGroup(uniqueGroupName, requestSize, template.build()));
        long finishTime = System.currentTimeMillis();
        numSuccess += succ.size();
      } catch (RunNodesException ex) {
        addSuccessAndLostNodes(ex, succ, failedNodes);

        numSuccess += succ.size();
        numFailed += failedNodes.size();
      } catch (AWSResponseException e) {
        if ("InstanceLimitExceeded".equals(e.getError().getCode())) {
          throw new KaramelException("It seems your ec2 account has instance limit.. if thats the case either decrease "
              + "size of your cluster or increase the limitation of your account.", e);
        } else if ("RequestLimitExceeded".equals(e.getError().getCode())) {
          logger.warn("RequestLimitExceeded. Can recover from it by sleeping longer between requests.");
        } else if ("InsufficientInstanceCapacity".equals(e.getError().getCode())) {
          logger.warn(
              "InsufficientInstanceCapacity. Can recover from it, by reducing the number of instances in the request, "
              + "or waiting for additional capacity to become available");
        } else {
          logger.error(e.getMessage(), e);
        }
      } catch (IllegalStateException ex) {
        logger.error("", ex);
        logger.info(String.format("#%d Hurry up EC2!! I want machines for %s, will ask you again in %d ms :@", tries,
            uniqueGroupName, Settings.AWS_RETRY_INTERVAL), ex);
      }

      unforkedVmNames = findLeftVmNames(succ, unforkedVmNames);
      successfulNodes.addAll(succ);
      sanityCheckSuccessfulNodes(successfulNodes, failedNodes);
      if (successfulNodes.size() < numberToLaunch) {
        try {
          succeed = false;
          logger.info(String.format("So far we got %d successful-machine(s) and %d failed-machine(s) out of %d "
              + "original-number for '%s'. Failed nodes will be killed later.", successfulNodes.size(),
              failedNodes.size(),
              numberToLaunch, uniqueGroupName));
          Thread.currentThread().sleep(Settings.AWS_RETRY_INTERVAL);
        } catch (InterruptedException ex1) {
          logger.error("", ex1);
        }
      } else {
        succeed = true;
        logger.info(String.format("Cool!! we got all %d machine(s) for '%s' |;-) we have %d failed-machines to kill "
            + "before we go on..", numberToLaunch, uniqueGroupName, failedNodes.size()));
        if (failedNodes.size() > 0) {
          cleanupFailedNodes(failedNodes);
        }
        List<MachineRuntime> machines = new ArrayList<>();
        for (NodeMetadata node : successfulNodes) {
          if (node != null) {
            MachineRuntime machine = new MachineRuntime(mainGroup);
            ArrayList<String> privateIps = new ArrayList();
            ArrayList<String> publicIps = new ArrayList();
            privateIps.addAll(node.getPrivateAddresses());
            publicIps.addAll(node.getPublicAddresses());
            machine.setMachineType("ec2/" + ec2.getRegion() + "/" + ec2.getType() + "/" + ec2.getAmi() + "/"
                + ec2.getVpc() + "/" + ec2.getPrice());
            machine.setVmId(node.getId());
            machine.setName(node.getName());
            // we check availability of ip addresses in the sanitycheck
            machine.setPrivateIp(privateIps.get(0));
            machine.setPublicIp(publicIps.get(0));
            machine.setSshPort(node.getLoginPort());
            machine.setSshUser(ec2.getUsername());
            machines.add(machine);
          }
        }

        // Report aggregrate results
        // timeTaken (#machines, #batchSize)
        // numSuccess, numFailed, numberToLaunch, InstanceType, **RequestLimitExceeded, **InsufficientInstanceCapacity,
        // RequestResourceCountExceeded, ResourceCountExceeded, InsufficientAddressCapacity**
        // If your requests have been throttled, you'll get the following error: Client.RequestLimitExceeded. For more
        //information, see Query API Request Rate.
        //  http://docs.aws.amazon.com/AWSEC2/latest/APIReference/query-api-troubleshooting.html#api-request-rate
        // InvalidInstanceID.NotFound, InvalidGroup.NotFound -> eventual consistency
        // Spot instances: MaxSpotInstanceCountExceeded
        // Groups of Spot instances: MaxSpotFleetRequestCountExceeded
        // http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/spot-fleet.html#spot-fleet-limitations
        return machines;
      }

    }
    // Report aggregrate results
    throw new KaramelException(String.format("Couldn't fork machines for group'%s'", mainGroup.getName()));
  }

  private void cleanupFailedNodes(Map<NodeMetadata, Throwable> failedNodes) {
    if (failedNodes.size() > 0) {
      Set<String> lostIds = Sets.newLinkedHashSet();
      for (Map.Entry<NodeMetadata, Throwable> lostNode : failedNodes.entrySet()) {
        lostIds.add(lostNode.getKey().getId());
      }
      logger.info(String.format("Destroying failed nodes with ids: %s", lostIds.toString()));
      Set<? extends NodeMetadata> destroyedNodes = context.getComputeService().destroyNodesMatching(
          Predicates.in(failedNodes.keySet()));
      lostIds.clear();
      for (NodeMetadata destroyed : destroyedNodes) {
        lostIds.add(destroyed.getId());
      }
      logger.info("Failed nodes destroyed ;)");
    }
  }

  private void sanityCheckSuccessfulNodes(Set<NodeMetadata> successfulNodes,
      Map<NodeMetadata, Throwable> lostNodes) {
    logger.info(String.format("Sanity check on successful nodes... "));
    List<NodeMetadata> tmpNodes = new ArrayList<>();
    tmpNodes.addAll(successfulNodes);
    successfulNodes.clear();
    for (int i = 0; i < tmpNodes.size(); i++) {
      NodeMetadata nm = tmpNodes.get(i);
      if (nm == null) {
        logger.info("for some reason one of the nodes is null, we removed it from the successfull nodes");
      } else if (nm.getPublicAddresses() == null || nm.getPublicAddresses().isEmpty()) {
        logger.info("Ec2 hasn't assined public-ip address to a node, we remove it from successfull nodes");
        lostNodes.put(nm, new KaramelException("No public-ip assigned"));
      } else if (nm.getPrivateAddresses() == null || nm.getPrivateAddresses().isEmpty()) {
        logger.info("Ec2 hasn't assined private-ip address to a node, we remove it from successfull nodes");
        lostNodes.put(nm, new KaramelException("No private-ip assigned"));
      } else {
        successfulNodes.add(nm);
      }
    }
  }

  private void addSuccessAndLostNodes(RunNodesException rnex, Set<NodeMetadata> successfulNodes,
      Map<NodeMetadata, Throwable> lostNodes) {
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

  @Override
  public void cleanup(JsonCluster definition, ClusterRuntime runtime) throws KaramelException {
    runtime.resolveFailures();
    List<GroupRuntime> groups = runtime.getGroups();
    Set<String> allEc2Vms = new HashSet<>();
    Set<String> allEc2VmsIds = new HashSet<>();
    Map<String, String> groupRegion = new HashMap<>();
    for (GroupRuntime group : groups) {
      group.getCluster().resolveFailures();
      Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      if (provider instanceof Ec2) {
        for (MachineRuntime machine : group.getMachines()) {
          if (machine.getVmId() != null) {
            allEc2VmsIds.add(machine.getVmId());
          }
        }
        JsonGroup jg = UserClusterDataExtractor.findGroup(definition, group.getName());
        List<String> vmNames = Settings.AWS_UNIQUE_VM_NAMES(group.getCluster().getName(), group.getName(),
            1, jg.getSize());
        allEc2Vms.addAll(vmNames);
        groupRegion.put(group.getName(), ((Ec2) provider).getRegion());
      }
    }
    cleanup(definition.getName(), allEc2VmsIds, allEc2Vms, groupRegion);
  }

  public void cleanup(String clusterName, Set<String> vmIds, Set<String> vmNames, Map<String, String> groupRegion)
      throws KaramelException {
    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    }
    Set<String> groupNames = new HashSet<>();
    for (Map.Entry<String, String> gp : groupRegion.entrySet()) {
      groupNames.add(Settings.AWS_UNIQUE_GROUP_NAME(clusterName, gp.getKey()));
    }
    logger.info(String.format("Killing following machines with names: \n %s \nor inside group names %s \nor with ids: "
        + "%s", vmNames.toString(), groupNames, vmIds));
    logger.info(String.format("Killing all machines in groups: %s", groupNames.toString()));
    context.getComputeService().destroyNodesMatching(withPredicate(vmIds, vmNames, groupNames));
    logger.info(String.format("All machines destroyed in all the security groups. :) "));
    for (Map.Entry<String, String> gp : groupRegion.entrySet()) {
      String uniqueGroupName = Settings.AWS_UNIQUE_GROUP_NAME(clusterName, gp.getKey());
      for (SecurityGroup secgroup : context.getSecurityGroupApi().describeSecurityGroupsInRegion(gp.getValue())) {
        if (secgroup.getName().startsWith("jclouds#" + uniqueGroupName) || secgroup.getName().equals(uniqueGroupName)) {
          logger.info(String.format("Destroying security group '%s' ...", secgroup.getName()));
          boolean retry = false;
          int count = 0;
          do {
            count++;
            try {
              logger.info(String.format("#%d Destroying security group '%s' ...", count, secgroup.getName()));
              ((AWSSecurityGroupApi) context.getSecurityGroupApi()).deleteSecurityGroupInRegionById(gp.getValue(),
                  secgroup.getId());
            } catch (IllegalStateException ex) {
              Throwable cause = ex.getCause();
              if (cause instanceof AWSResponseException) {
                AWSResponseException e = (AWSResponseException) cause;
                if (e.getError().getCode().equals("InvalidGroup.InUse") || e.getError().getCode().
                    equals("DependencyViolation")) {
                  logger.info(String.format("Hurry up EC2!! terminate machines!! '%s', will retry in %d ms :@",
                      uniqueGroupName, Settings.AWS_RETRY_INTERVAL));
                  retry = true;
                  try {
                    Thread.currentThread().sleep(Settings.AWS_RETRY_INTERVAL);
                  } catch (InterruptedException ex1) {
                    logger.error("", ex1);
                  }
                } else {
                  throw ex;
                }
              }
            }
          } while (retry);
          logger.info(String.format("The security group '%s' destroyed ^-^", secgroup.getName()));
        }
      }
    }
  }

  public static Predicate<NodeMetadata> withPredicate(final Set<String> ids, final Set<String> names,
      final Set<String> groupNames) {
    return new Predicate<NodeMetadata>() {
      @Override
      public boolean apply(NodeMetadata nodeMetadata) {
        String id = nodeMetadata.getId();
        String name = nodeMetadata.getName();
        String group = nodeMetadata.getGroup();
        return ((id != null && ids.contains(id)) || (name != null && names.contains(name)
            || (group != null && groupNames.contains(group))));
      }

      @Override
      public String toString() {
        return "machines predicate";
      }
    };
  }
}
