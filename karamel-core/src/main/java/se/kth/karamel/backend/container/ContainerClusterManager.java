package se.kth.karamel.backend.container;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkCreation;
import com.spotify.docker.client.messages.ContainerCreation;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.container.task.DownloadImageTask;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.NodeRunTime;
import se.kth.karamel.client.api.CookbookCache;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.json.JsonRecipe;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ContainerClusterManager {

  private static final Logger logger = Logger.getLogger(ContainerClusterManager.class);

  private HashMap<String, List<String>> containerHostMap = new HashMap<>();

  /**
   * Mapping for Containers and for Groups
   */
  private HashMap<String, ArrayList<NodeRunTime>> containerGroupMap = new HashMap<>();

  /**
   * List of Docker Clients
   */
  private HashMap<String, DockerClient> dockerClientMap = new HashMap<>();

  /**
   * List of Docker Host Machines
   */
  private List<NodeRunTime> hostMachineRuntimes = new ArrayList<>();

  /**
   * This maintains the list of Group runtimes dedicated to containers. In this list Container Host Group
   * is filtered out
   */
  List<GroupRuntime> containerGroupRuntimes = new ArrayList<>();

  /**
   * Thread group for executor service
   */
  private ExecutorService workerPool = Executors.newCachedThreadPool();

  private ClusterRuntime runtime;
  private JsonCluster cluster;
  private ClusterStats clusterStats;
  private TaskSubmitter taskSubmitter;
  private int numOfContainers = 0;

  public ContainerClusterManager(ClusterRuntime runtime, JsonCluster cluster, ClusterStats clusterstats,
                                 TaskSubmitter taskSubmitter) {
    this.runtime = runtime;
    this.cluster = cluster;
    this.taskSubmitter = taskSubmitter;
    this.clusterStats = clusterstats;
  }

  public HashMap<String, ArrayList<NodeRunTime>> startContainers() throws KaramelException, InterruptedException,
    DockerException {
    for (JsonGroup jsonGroup : cluster.getGroups()) {
      if (!Settings.CONTAINER_HOST_GROUP.equals(jsonGroup.getName())) {
        numOfContainers += jsonGroup.getSize();
        containerGroupMap.put(jsonGroup.getName(), new ArrayList<NodeRunTime>());
      }
    }

    List<NodeRunTime> machines = new ArrayList<>();

    int containerOffset = 0;

    for (String groupName : containerGroupMap.keySet()) {
      JsonGroup group = UserClusterDataExtractor.findGroup(cluster, groupName);

      for (int i = 0; i < group.getSize(); i++) {

        int position = containerOffset % hostMachineRuntimes.size();
        NodeRunTime hostMachine = hostMachineRuntimes.get(position);

        int sshPort = 11000 + containerOffset;
        String publicIp = hostMachine.getPublicIp();

        DockerClient client = dockerClientMap.get(publicIp);

        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        List<PortBinding> hostPorts = new ArrayList<>();
        hostPorts.add(PortBinding.of("0.0.0.0", sshPort));
        portBindings.put("22", hostPorts);

        //TODO: exposing all the ports found in links in every container, We might not want to do that. and only need
        // to expose relevant port for containers.
        String[] clusterLinks = UserClusterDataExtractor.clusterLinks(cluster, runtime).split("\n");
        List<String> ports = new ArrayList();

        for (int j = 0; j < clusterLinks.length; j++) {
          ports.add(clusterLinks[j].split("//")[1].split("/")[0].split(":")[1]);
        }

        for (String port : ports) {
          List<PortBinding> randomHostPorts = new ArrayList<PortBinding>();
          randomHostPorts.add(PortBinding.randomPort("0.0.0.0"));
          portBindings.put(port, randomHostPorts);
        }

        //adding SSH port to expose
        ports.add("22");

        HostConfig hostConfig = HostConfig.builder()
          .networkMode("karamel")
          .portBindings(portBindings)
          .binds("/var/repository:/tmp/binary")
          .build();

        String[] exposedPorts = new String[ports.size()];
        exposedPorts = ports.toArray(exposedPorts);

        String containerName = "node" + containerOffset;

        ContainerConfig containerConfig = ContainerConfig.builder()
          .image("shelan/karamel-node:v3.0.0")
          .hostConfig(hostConfig)
          .exposedPorts(exposedPorts)
          .hostname(containerName)
          //TODO : This is a hardcoded value specific to hadoop server directory
          .volumes("/srv")
          .build();


        // Retry mechanism
        boolean succeded = false;
        String containerId = "";
        while (!succeded) {
          try {
            final ContainerCreation creation = client.createContainer(containerConfig, containerName);
            containerId = creation.id();

          } catch (Exception e) {
            logger.error(e);
            logger.info("Retrying forking containers");
            Thread.sleep(200);
            continue;
          }
          succeded = true;
        }

        // Retry mecahnism
        succeded = false;
        String containerIp = "";
        while (!succeded) {
          try {
            client.startContainer(containerId);
            containerIp = client.inspectContainer(containerId).networkSettings().networks().get("karamel")
              .ipAddress();
          } catch (Exception e) {
            logger.error(e);
            logger.info("Retrying forking containers");
            Thread.sleep(200);
            continue;
          }
          succeded = true;
        }

        NodeRunTime containerRuntime = new NodeRunTime(hostMachine.getGroup());
        containerRuntime.setNodeType(NodeRunTime.NodeType.CONTAINER);
        containerRuntime.setMachineType(NodeRunTime.NodeType.CONTAINER.name());
        containerRuntime.setName(containerName);
        containerRuntime.setVmId(containerName);
        containerRuntime.setContainerId(containerName);
        containerRuntime.setPrivateIp(containerIp);
        containerRuntime.setPublicIp(publicIp);
        containerRuntime.setSshPort(sshPort);
        containerRuntime.setSshUser("vagrant");
        machines.add(containerRuntime);
        containerGroupMap.get(groupName).add(containerRuntime);
        containerHostMap.get(publicIp).add(containerName);

        containerOffset++;
      }
    }
    return containerGroupMap;
  }

  public void init() {
    for (GroupRuntime groupRuntime : runtime.getGroups()) {
      if (Settings.CONTAINER_HOST_GROUP.equals(groupRuntime.getName())) {
        // this is the host group lets add all the machines to the host machines list
        this.hostMachineRuntimes.addAll(groupRuntime.getMachines());
      }
    }

    CountDownLatch countDown = new CountDownLatch(hostMachineRuntimes.size());

    for (NodeRunTime nodeRunTime : hostMachineRuntimes) {
      String publicIp = nodeRunTime.getPublicIp();
      containerHostMap.put(publicIp, new ArrayList<String>());

      DockerClient docker = DefaultDockerClient.builder().uri("http://" + nodeRunTime.getPublicIp() + ":2375")
        .readTimeoutMillis(90000)
        .connectTimeoutMillis(90000)
        .build();
      AuthConfig authConfig = AuthConfig.builder().serverAddress("https://index.docker.io/v1/").build();
      try {
        docker.auth(authConfig);
        //pulling all the required images here.
        DownloadImageTask downloadImageTask = new DownloadImageTask(docker, countDown);
        workerPool.submit(downloadImageTask);

      } catch (DockerException e) {
        logger.error("Error while initializing docker clients", e);
      } catch (InterruptedException e) {
        logger.error("Interrupted while initializing docker clients", e);
      }
      dockerClientMap.put(publicIp, docker);
    }
    try {
      countDown.await();
    } catch (InterruptedException e) {
      logger.error("Interrupred", e);
    }
    logger.info("Downloaded images for docker hosts");
  }

  public int getNOfContainers() {
    return numOfContainers;
  }

  //TODO: this is the core logic to parse existing port should be removed if not used in future.
  public void extractPorts() throws KaramelException {
    for (JsonGroup jsonGroup : cluster.getGroups()) {
      for (JsonCookbook jsonCookbook : jsonGroup.getCookbooks()) {
        for (JsonRecipe jsonRecipe : jsonCookbook.getRecipes()) {
          String cbid = jsonCookbook.getId();
          KaramelizedCookbook cb = CookbookCache.get(cbid);
          MetadataRb metadataRb = cb.getMetadataRb();
        }
      }
    }
  }

  public void setupNetworking(String kvStorePublicIP, String kvStorePrivateIP) throws DockerException,
    InterruptedException {
    DockerClient client = dockerClientMap.get(kvStorePublicIP);

    client.pull("progrium/consul:latest", AuthConfig.builder().build());
    final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
    List<PortBinding> hostPorts = new ArrayList<PortBinding>();
    hostPorts.add(PortBinding.of("0.0.0.0", 8500));
    portBindings.put("8500", hostPorts);
    HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
    ContainerConfig containerConfig = ContainerConfig.builder().image("progrium/consul:latest")
      .hostConfig(hostConfig)
      .cmd("-server", "-bootstrap")
      .exposedPorts("8500")
      .build();

    final ContainerCreation creation = client.createContainer(containerConfig);
    final String id = creation.id();

    client.startContainer(id);

    //Ipam ipam = Ipam.builder().config("10.0.4.0/24","10.0.4.0/24","10.0.4.255").build();
    NetworkConfig networkConfig = NetworkConfig.builder().driver("overlay").name("karamel").build();
    NetworkCreation networkCreation = null;

    while (networkCreation == null || !(networkCreation.id().length() > 0)) {
      try {
        networkCreation = client.createNetwork(networkConfig);
      } catch (Exception e) {
        Thread.sleep(1000);
      }
    }
  }

  public HashMap<String, ArrayList<NodeRunTime>> restartContainers() throws KaramelException, DockerException,
    InterruptedException {
    destroyContainers();
    return startContainers();

  }

  private void destroyContainers() throws InterruptedException {
    for (String ip : containerHostMap.keySet()) {
      DockerClient client = dockerClientMap.get(ip);
      for (String containerName : containerHostMap.get(ip)) {
        try {
          client.killContainer(containerName);
          client.removeContainer(containerName);
        } catch (DockerException e) {
          logger.error("error while stopping container " + containerName, e);
        }
      }
    }
  }
}
