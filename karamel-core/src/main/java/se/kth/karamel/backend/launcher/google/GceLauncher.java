package se.kth.karamel.backend.launcher.google;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.domain.AttachDisk;
import org.jclouds.googlecomputeengine.domain.Firewall;
import org.jclouds.googlecomputeengine.domain.Instance;
import org.jclouds.googlecomputeengine.domain.Instance.Scheduling;
import org.jclouds.googlecomputeengine.domain.Metadata;
import org.jclouds.googlecomputeengine.domain.NewInstance;
import org.jclouds.googlecomputeengine.domain.Operation;
import org.jclouds.googlecomputeengine.domain.Zone;
import org.jclouds.googlecomputeengine.features.InstanceApi;
import org.jclouds.googlecomputeengine.features.NetworkApi;
import org.jclouds.googlecomputeengine.features.OperationApi;
import org.jclouds.googlecomputeengine.options.FirewallOptions;
import org.jclouds.googlecomputeengine.options.NetworkCreationOptions;
import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.launcher.Launcher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.util.GceSettings;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.exception.InvalidCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.UnsupportedImageType;

/**
 *
 * @author Hooman
 */
public class GceLauncher extends Launcher {

  private static final int DEFAULT_SSH_PORT = 22;
  private static final String GCE_PROVIDER = "gce";
  private static final Logger logger = Logger.getLogger(GceLauncher.class);
  public final GceContext context;
  public final SshKeyPair sshKeyPair;

  public GceLauncher(GceContext context, SshKeyPair sshKeyPair) {
    this.context = context;
    this.sshKeyPair = sshKeyPair;
  }

  /**
   *
   * @param jsonKeyPath
   * @return
   */
  public static Credentials readCredentials(String jsonKeyPath) {
    Credentials credentials = null;
    if (jsonKeyPath != null && !jsonKeyPath.isEmpty()) {
      try {
        String fileContents = Files.toString(new File(jsonKeyPath), Charset.defaultCharset());
        Supplier<Credentials> credentialSupplier = new GoogleCredentialsFromJson(fileContents);
        credentials = credentialSupplier.get();
      } catch (IOException ex) {
        logger.error("Error Reading the Json key file. Please check the provided path is correct.", ex);
      }
    }
    return credentials;
  }

  /**
   *
   * @param credentials
   * @return
   * @throws InvalidCredentialsException
   */
  public static GceContext validateCredentials(Credentials credentials) throws InvalidCredentialsException {
    try {
      GceContext context = new GceContext(credentials);
      GoogleComputeEngineApi gceApi = context.getGceApi();
      String projectName = gceApi.project().get().name();
      context.setProjectName(projectName);
      logger.info(String.format("Sucessfully Authenticated to project %s", projectName));
      return context;
    } catch (AuthorizationException e) {
      throw new InvalidCredentialsException("accountid:" + credentials.identity, e);
    }
  }

  @Override
  public String forkGroup(JsonCluster definition, ClusterRuntime runtime, String groupName) throws KaramelException {
    //JsonGroup jg = UserClusterDataExtractor.findGroup(definition, groupName);
    //Set<String> ports = new HashSet<>();
    //ports.addAll(Settings.AWS_VM_PORTS_DEFAULT);
    // TODO: assign arbitrary ip range.
    //String groupId = createFirewall(definition.getName(), jg.getName(),
    //    Settings.GCE_DEFAULT_IP_RANGE, ports);

    //Don't create a network, instead use the vpc network supplied or the
    //default one if the supplied network doesn't exist otherwise through an
    //exception
    logger.info("GCE fork group " + groupName);
    Gce gce = (Gce) UserClusterDataExtractor.getGroupProvider(definition, groupName);
    String vpcNetwork = gce.getVpc();
    if (context.getNetworkApi().get(vpcNetwork) == null) {
      logger.warn("VPC network (" + vpcNetwork + ") doesn't exist, fallback to " + "default network");
      vpcNetwork = GceSettings.DEFAULT_NETWORK_NAME;
      if (context.getNetworkApi().get(vpcNetwork) == null) {
        throw new KaramelException("The vpc network provided (" + vpcNetwork + ") and the default one don't exist");
      }
      definition.getGce().setVpc(vpcNetwork);
    }
    return vpcNetwork;
  }

  // TODO: Tags can be added.
  public String createFirewall(String clusterName, String groupName, String ipRange, Set<String> ports)
      throws KaramelException {
    String networkName = Settings.UNIQUE_GROUP_NAME(GCE_PROVIDER, clusterName, groupName);
    NetworkApi netApi = context.getNetworkApi();
    Operation createNetOp = netApi.create(NetworkCreationOptions.create(networkName, null, null, null, true));
    if (waitForOperation(context.getGceApi().operations(), createNetOp) == 1) {
      throw new KaramelException("Failed to create network with name " + networkName);
    }
    try {
      URI networkUri = GceSettings.buildNetworkUri(context.getProjectName(), networkName);
      List<Operation> operations = new ArrayList<>(ports.size());
      for (String port : ports) {
        String p;
        String pr;
        if (port.contains("/")) {
          String[] s = port.split("/");
          p = s[0];
          pr = s[1];
        } else {
          p = port;
          pr = "tcp";
        }
        FirewallOptions firewall = new FirewallOptions()
            .addAllowedRule(Firewall.Rule.create(pr, ImmutableList.of(p)))
            .addSourceRange("0.0.0.0/0");
        String fwName = Settings.GCE_UNIQUE_FIREWALL_NAME(networkName, p, pr);
        operations.add(context.getFireWallApi().createInNetwork(fwName, networkUri, firewall));
        logger.info(String.format("Ports became open for '%s'", networkName));
      }

      for (Operation op : operations) {
        // TODO: Handle failed operations and report them.
        waitForOperation(context.getGceApi().operations(), op);
      }
    } catch (URISyntaxException ex) {
      logger.error(ex.getMessage(), ex);
    }

    return networkName;
  }

  @Override
  public List<MachineRuntime> forkMachines(JsonCluster definition, ClusterRuntime runtime, String groupName)
      throws KaramelException {
    Gce gce = (Gce) UserClusterDataExtractor.getGroupProvider(definition, groupName);
    JsonGroup definedGroup = UserClusterDataExtractor.findGroup(definition, groupName);
    GroupRuntime group = UserClusterDataExtractor.findGroup(runtime, groupName);

    return forkMachines(group, definedGroup.getSize(), gce);
  }

  public List<MachineRuntime> forkMachines(GroupRuntime group, int totalSize, Gce gce) {
    List<MachineRuntime> machines = new ArrayList<>(totalSize);
    try {
      URI machineType = GceSettings.buildMachineTypeUri(context.getProjectName(), gce.getZone(), gce.getType());
      URI networkType = GceSettings.buildNetworkUri(context.getProjectName(), gce.getVpc());
      URI subnetType = getSubnetType(gce);
      URI imageType = getImageType(gce);
      URI nvmeDiskType = getNVMeDiskType(gce);
      URI hddDiskType = getHDDDiskType(gce);
      URI ssdDiskType = getSSDDiskType(gce);
      
      String clusterName = group.getCluster().getName();
      String groupName = group.getName();
      boolean isPreemptible = gce.isPreemptible();
      String uniqeGroupName = Settings.UNIQUE_GROUP_NAME(GCE_PROVIDER, clusterName, groupName);
      List<String> allVmNames = Settings.UNIQUE_VM_NAMES(GCE_PROVIDER, clusterName, groupName, totalSize);
      ArrayList<Operation> operations = new ArrayList<>(totalSize);
      logger.info(String.format("Start forking %d machine(s) for '%s' ...", totalSize, uniqeGroupName));
      InstanceApi instanceApi = context.getGceApi().instancesInZone(gce.getZone());

      boolean autoRestart = false;

      for (String name : allVmNames) {
        AttachDisk disk = AttachDisk.create(AttachDisk.Type.PERSISTENT, null,
            null, null, true, AttachDisk.InitializeParams.create(null, gce
                .getDiskSize(), imageType, null), true, null, null);
  
        List<AttachDisk> disks = Lists.newArrayList(disk);
        //NVMe
        for(int i=0; i<gce.getNvme(); i++){
          AttachDisk nvme = AttachDisk.create(AttachDisk.Type.SCRATCH,
              AttachDisk.Mode.READ_WRITE, null, null, false,
              AttachDisk.InitializeParams.create(null,null,
                  null, nvmeDiskType), true, null,
              AttachDisk.DiskInterface.NVME);
          disks.add(nvme);
        }
        //HDD
        for(int i=0; i<gce.getHdd(); i++){
          AttachDisk hdd = AttachDisk.create(AttachDisk.Type.PERSISTENT, null,
              null, null, false, AttachDisk.InitializeParams.create(null, gce
                  .getDiskSize(), null, hddDiskType), true, null, null);
          disks.add(hdd);
        }
        //SSD
        for(int i=0; i<gce.getSsd(); i++){
          AttachDisk ssd = AttachDisk.create(AttachDisk.Type.PERSISTENT, null,
              null, null, false, AttachDisk.InitializeParams.create(null, gce
                  .getDiskSize(), null, ssdDiskType), true, null, null);
          disks.add(ssd);
        }
        
        Scheduling scheduling;
        if (isPreemptible) {
          scheduling = Scheduling.create(Scheduling.OnHostMaintenance.TERMINATE, false, true);
        } else {
          scheduling = Scheduling.create(Scheduling.OnHostMaintenance.MIGRATE, false, false);
        }
        NewInstance.Builder builder = new NewInstance.Builder(name, machineType, networkType, subnetType,
            disks).scheduling(scheduling);
        Operation operation = instanceApi.create(builder.build());
//        Operation operation = instanceApi.create(NewInstance.create(name,
//            machineType, networkType, subnetType, Lists.newArrayList(disk), null, null));
//        instanceApi.setScheduling(name, Instance.Scheduling.OnHostMaintenance.MIGRATE, autoRestart, isPreemptible);
        logger.info("Starting instance " + name);
        operations.add(operation);
      }
      ArrayList<Operation> metadataOperations = new ArrayList<>(totalSize);
      for (int i = 0; i < totalSize; i++) {
        if (waitForOperation(context.getGceApi().operations(), operations.get(i)) == 0) {
          Instance vm = instanceApi.get(allVmNames.get(i));
          Metadata metadata;
          if (vm.metadata() != null) {
            metadata = vm.metadata().clone();
          } else {
            metadata = Metadata.create();
          }
          // Username given for provider is used as SSH user for VMs. 
          metadata.put("sshKeys", gce.getUsername() + ":" + sshKeyPair.getPublicKey());
          metadataOperations.add(instanceApi.setMetadata(allVmNames.get(i), metadata));
          MachineRuntime machine = new MachineRuntime(group);
          machine.setMachineType("gce/" + gce.getZone() + "/" + gce.getType() + "/" + gce.getImage());
          machine.setVmId(vm.id());
          machine.setName(vm.name());
          Instance.NetworkInterface netInterface = vm.networkInterfaces().get(0);
          machine.setPrivateIp(netInterface.networkIP());
          machine.setPublicIp(netInterface.accessConfigs().get(0).natIP());
          machines.add(machine);
          machine.setSshPort(DEFAULT_SSH_PORT);
          machine.setSshUser(gce.getUsername());
          machine.setLocationDomainId(getLocationDomainId(gce));
        }
      }
      // TODO handle failure for setting metadata using metadataOperations list.
    } catch (URISyntaxException ex) {
      logger.error("Wrong URI.", ex);
    } catch (UnsupportedImageType ex) {
      logger.error(ex.getMessage(), ex);
    }

    return machines;
  }

  @Override
  public void cleanup(JsonCluster definition, ClusterRuntime runtime) throws KaramelException {
    runtime.resolveFailures();
    List<GroupRuntime> groups = runtime.getGroups();
    Map<String, List<String>> vmZone = new HashMap<>();
    Set<String> networks = new HashSet<>();
    for (GroupRuntime group : groups) {
      group.getCluster().resolveFailures();
      Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      if (provider instanceof Gce) {
        networks.add(group.getName());
        Gce gce = (Gce) provider;
        List<String> vms;
        if (vmZone.containsKey(gce.getZone())) {
          vms = vmZone.get(gce.getZone());
        } else {
          vms = new LinkedList<>();
          vmZone.put(gce.getZone(), vms);
        }
        for (MachineRuntime machine : group.getMachines()) {
          if (machine.getName() != null) {
            vms.add(machine.getName());
          }
        }
      }
    }
    cleanup(vmZone, definition.getName(), networks);
  }

  /**
   *
   * @param vmZone
   * @param clusterName
   * @param groupNames
   * @throws KaramelException
   */
  public void cleanup(Map<String, List<String>> vmZone, String clusterName, Set<String> groupNames)
      throws KaramelException {
    Iterator<Map.Entry<String, List<String>>> iterator = vmZone.entrySet().iterator();
    LinkedList<Operation> operations = new LinkedList<>();
    while (iterator.hasNext()) {
      Map.Entry<String, List<String>> entry = iterator.next();
      String zone = entry.getKey();
      List<String> vms = entry.getValue();
      logger.info(String.format("Killing following machines with names: \n %s.", vms.toString()));
      InstanceApi instanceApi = context.getGceApi().instancesInZone(zone);
      for (String vm : vms) {
        Operation op = instanceApi.delete(vm);
        if (op != null) {
          operations.add(op);
        }
      }
    }

    for (Operation operation : operations) {
      if (waitForOperation(context.getGceApi().operations(), operation) == 1) {
        logger.warn(String.format("%s operation has timedout: %s\n",
            operation.operationType(), operation.httpErrorMessage()));
      } else {
        logger.info(String.format("Operation %s  was successfully done on %s\n.",
            operation.operationType(), operation.targetLink()));
      }
    }

    // TODO: Handle the operations failures situation.
    operations.clear();
    /*
     * NetworkApi netApi = context.getNetworkApi();
     * FirewallApi fwApi = context.getFireWallApi();
     * RouteApi routeApi = context.getRouteApi();
     * //Delete network firewalls and routes first and then delete network, Otherwise network deletion will not work.
     * for (String group : groupNames) {
     * String networkName = Settings.UNIQUE_GROUP_NAME(GCE_PROVIDER, clusterName, group);
     * Network network = netApi.get(networkName);
     * if (network != null) {
     * URI networkUri = network.selfLink();
     * Iterator<ListPage<Firewall>> fwIterator = fwApi.list();
     * while (fwIterator.hasNext()) {
     * ListPage<Firewall> page = fwIterator.next();
     * for (Firewall fw : page) {
     * if (fw.network().equals(networkUri)) {
     * operations.add(fwApi.delete(fw.name()));
     * }
     * }
     * }
     * }
     * }
     *
     * for (Operation operation : operations) {
     * if (waitForOperation(context.getGceApi().operations(), operation) == 1) {
     * logger.warn(String.format("%s operation has timedout: %s\n",
     * operation.operationType(), operation.httpErrorMessage()));
     * } else {
     * logger.info(String.format("Operation %s was successfully done on %s\n.",
     * operation.operationType(), operation.targetLink()));
     * }
     * }
     *
     * operations.clear();
     *
     * for (String group : groupNames) {
     * String networkName = Settings.UNIQUE_GROUP_NAME(GCE_PROVIDER, clusterName, group);
     * Operation op = netApi.delete(networkName);
     * if (op != null) {
     * operations.add(op);
     * } else {
     * logger.info(String.format("Network %s does not exist.", networkName));
     * }
     * }
     *
     * for (Operation operation : operations) {
     * if (waitForOperation(context.getGceApi().operations(), operation) == 1) {
     * logger.warn(String.format("%s operation has timedout: %s\n",
     * operation.operationType(), operation.httpErrorMessage()));
     * } else {
     * logger.info(String.format("Operation %s was successfully done on %s\n.",
     * operation.operationType(), operation.targetLink()));
     * }
     * }
     */
  }

  private static int waitForOperation(OperationApi api, Operation operation) {
    // TODO: configurable timeout.
    //  TODO: write this method using org.jclouds.util.Predicates2.retry;
    int timeout = 60; // seconds
    int time = 0;

    while (operation != null && operation.status() != Operation.Status.DONE) {
      if (time >= timeout) {
        return 1;
      }
      time++;
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        logger.warn(e);
      }

      operation = api.get(operation.selfLink());
    }
    return 0;
  }

  private URI getImageType(Gce gce)
      throws UnsupportedImageType, URISyntaxException {
    URI imageType = GceSettings.buildGlobalImageUri(gce.getImage());
    if (context.getGceApi().images().get(imageType) == null) {
      imageType = GceSettings.buildProjectImageUri(context.getProjectName(),
           gce.getImage());
    }
    return imageType;
  }

  private URI getSubnetType(Gce gce) throws URISyntaxException {
    if (gce.getSubnet() == null) {
      return null;
    }

    Zone zone = context.getGceApi().zones().get(gce.getZone());
    if (zone != null) {
      String regionUrl = zone.region();
      String region = regionUrl.substring(regionUrl.lastIndexOf("/") + 1,
          regionUrl.length());
      URI subnet = GceSettings.buildSubnetUri(context.getProjectName(),
          region, gce.getSubnet());
      if (context.getGceApi().subnetworksInRegion(region).get(gce
          .getSubnet()) != null) {
        return subnet;
      }
      logger.info("Subnet (" + gce.getSubnet() + ") does not exist in region " + "(" + region + ") ");
    }
    return null;
  }
  
  private URI getNVMeDiskType(Gce gce)
      throws URISyntaxException {
    return GceSettings.buildLocalSSDDiskTypeUri(context.getProjectName(),
        gce.getZone());
  }
  
  private URI getHDDDiskType(Gce gce)
      throws URISyntaxException {
    return GceSettings.buildHDDDiskTypeUri(context.getProjectName(),
        gce.getZone());
  }
  
  private URI getSSDDiskType(Gce gce)
      throws URISyntaxException {
    return GceSettings.buildSSDDiskTypeUri(context.getProjectName(),
        gce.getZone());
  }
  
  private int getLocationDomainId(Gce gce){
    switch (gce.getZone().charAt(gce.getZone().length() - 1)){
      case 'a':
        return 1;
      case 'b':
        return 2;
      case 'c':
        return 3;
      case 'd':
        return 4;
      case 'f':
        return 5;
    }
    return MachineRuntime.DEFAULT_LOCATION_DOMAIN_ID;
  }

}
