package se.kth.karamel.backend.launcher.google;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jclouds.compute.RunNodesException;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecomputeengine.domain.Firewall;
import org.jclouds.googlecomputeengine.features.FirewallApi;
import org.jclouds.googlecomputeengine.features.InstanceApi;
import org.jclouds.googlecomputeengine.features.NetworkApi;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.util.GceSettings;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.exception.InvalidCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author Hooman
 */
public class GceLauncherTest {

  static GceContext context;

  @Test
  public void dummyTest() {
    Assert.assertTrue(true);
  }

//  @BeforeClass
  public static void init() throws InvalidCredentialsException {
    String jsonKeyPath = Settings.KARAMEL_ROOT_PATH + File.separator + "gce-key.json";
    Credentials credentials = GceLauncher.readCredentials(jsonKeyPath);
    context = GceLauncher.validateCredentials(credentials);
  }

  /**
   * Test of readCredentials method, of class GceLauncher.
   */
//  @Test
  public void testReadCredentials() {
    String jsonKeyPath = Settings.KARAMEL_ROOT_PATH + File.separator + "gce-key.json";
    Credentials credentials = GceLauncher.readCredentials(jsonKeyPath);
    assert credentials != null;
    assert credentials.identity != null && !credentials.identity.isEmpty();
    assert credentials.credential != null && !credentials.credential.isEmpty();
  }

  /**
   * Test of validateCredentials method, of class GceLauncher.
   *
   * @throws se.kth.karamel.common.exception.InvalidCredentialsException
   */
//  @Test
  public void validateCredentials() throws InvalidCredentialsException {
    assert context != null;
  }

//  @Test
  public void testForkMachines()
      throws InvalidCredentialsException, RunNodesException, URISyntaxException, ValidationException {
    int size = 1;
    List<MachineRuntime> machines = forkMachines("c1", "g1", size, "europe-west1-b");
    assert machines.size() == size;
    for (MachineRuntime machine : machines) {
      assert machine.getId() != null && !machine.getId().isEmpty();
      assert machine.getName() != null && !machine.getName().isEmpty();
      assert machine.getPublicIp() != null && !machine.getPublicIp().isEmpty();
      assert machine.getPrivateIp() != null && !machine.getPrivateIp().isEmpty();
    }
  }

//  @Test
  public void testCleanUp() throws
      InvalidCredentialsException, RunNodesException, URISyntaxException, KaramelException, InterruptedException {
    int size = 1;
    String clusterName = "c1";
    String groupName = "g1";
    String zone = "europe-west1-b";
    List<MachineRuntime> machines = forkMachines(clusterName, groupName, size, zone);
    List<String> vms = new ArrayList<>(machines.size());
    for (MachineRuntime machine : machines) {
      vms.add(machine.getName());
    }
    Map<String, List<String>> vmZone = new HashMap<>();
    vmZone.put(zone, vms);
    GceLauncher launcher = new GceLauncher(context, new SshKeyPair());
    String networkName = launcher.createFirewall(
        clusterName, groupName, Settings.GCE_DEFAULT_IP_RANGE, ImmutableSet.of("22/tcp"));
    Thread.sleep(60000);
    launcher.cleanup(vmZone, clusterName, ImmutableSet.of(groupName));
    InstanceApi instanceApi = context.getGceApi().instancesInZone(zone);
    for (String vm : vms) {
      assert instanceApi.get(vm) == null;
    }
    NetworkApi netApi = context.getNetworkApi();
    assert netApi.get(networkName) == null;
  }

//  @Test
  public void testCreateFirewall() throws KaramelException {
    GceLauncher launcher = new GceLauncher(context, new SshKeyPair());
    String clusterName = "c1";
    String groupName = "g1";
    String p1 = "22";
    String p2 = "80";
    String pr = "tcp";
    String networkName = launcher.createFirewall(clusterName, groupName,
        Settings.GCE_DEFAULT_IP_RANGE, ImmutableSet.of(p1 + "/" + pr, p2 + "/" + pr));
    String fw1Name = Settings.GCE_UNIQUE_FIREWALL_NAME(networkName, p1, pr);
    String fw2Name = Settings.GCE_UNIQUE_FIREWALL_NAME(networkName, p2, pr);
    FirewallApi fwApi = context.getFireWallApi();
    Firewall fw = fwApi.get(fw1Name);
    assert fw != null && !fw.allowed().isEmpty()
        && !fw.allowed().get(0).ports().isEmpty() && fw.allowed().get(0).ports().get(0).equalsIgnoreCase(p1);
    fw = fwApi.get(fw2Name);
    assert fw != null && !fw.allowed().isEmpty()
        && !fw.allowed().get(0).ports().isEmpty() && fw.allowed().get(0).ports().get(0).equalsIgnoreCase(p2);
  }

  private List<MachineRuntime> forkMachines(String clusterName, String groupName, int size, String zone)
      throws InvalidCredentialsException, RunNodesException, ValidationException {
    SshKeyPair keypair = new SshKeyPair();
    //TODO: read the public key from the configured path.
    keypair.setPublicKey("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCl4GSkn3cO2pl18ZJiPE9wKmoSJRDuL+JFoVQlrmpcw/"
        + "a2tcvS1Bjf2YlNVdDpDQ8vYNgkQjuZ4f2O7dDeMC/vi9eHOe3xZxBvTGpZREkKrQrUg9VfVYriYo8VvyPlXnRbim4wr9yPGdo"
        + "YPMRBoXkheGQiAI7pk7OG0JjLp8Jm0keBQb/J/Lbe/2zFIi/zQzmOPliNs7HVV/4R/QytmYpJyhZU3mJIhiWC7Hu1lZqMAJco"
        + "GRuhkisQt0VYoOZC8wgAkkthloOXamKztraG2Azseohk7sHiBEHsUdlxgivIM9ItUqa1x/4xI9u/AIztaPJrJiy2Syi3kZc0oe56G9WZ");
    Gce gce = new Gce();
    gce.setImage("ubuntu-1404-trusty-v20150316");
    gce.setZone(zone);
    gce.setType(GceSettings.MachineType.n1_standard_1.toString());
    gce.setUsername("hooman");
    GceLauncher launcher = new GceLauncher(context, keypair);
    ClusterRuntime cluster = new ClusterRuntime(clusterName);
    JsonGroup jsonGroup = new JsonGroup();
    jsonGroup.setName(groupName);
    GroupRuntime group = new GroupRuntime(cluster, jsonGroup);

    List<MachineRuntime> machines = launcher.forkMachines(group, size, gce);

    return machines;
  }
}
