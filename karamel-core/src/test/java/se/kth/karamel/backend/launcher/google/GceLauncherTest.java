package se.kth.karamel.backend.launcher.google;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.jclouds.compute.RunNodesException;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecomputeengine.domain.Firewall;
import org.jclouds.googlecomputeengine.features.FirewallApi;
import org.jclouds.googlecomputeengine.features.InstanceApi;
import org.jclouds.googlecomputeengine.options.ListOptions;
import org.junit.BeforeClass;
import org.junit.Test;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.client.model.Gce;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.InvalidCredentialsException;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author Hooman
 */
public class GceLauncherTest {

  private static final String PROJECT_NAME = "fine-blueprint-95313";
  static GceContext context;

  @BeforeClass
  public static void init() throws InvalidCredentialsException {
    Confs confs = Confs.loadKaramelConfs();
    confs.put(Settings.GCE_JSON_KEY_FILE_PATH, Settings.KARAMEL_ROOT_PATH + File.separator + "gce-key.json");
    Credentials credentials = GceLauncher.readCredentials(confs);
    context = GceLauncher.validateCredentials(credentials);
  }

  /**
   * Test of readCredentials method, of class GceLauncher.
   */
  @Test
  public void testReadCredentials() {
    Confs confs = Confs.loadKaramelConfs();
    confs.put(Settings.GCE_JSON_KEY_FILE_PATH, Settings.KARAMEL_ROOT_PATH + File.separator + "gce-key.json");
    Credentials credentials = GceLauncher.readCredentials(confs);
    assert credentials != null;
    assert credentials.identity != null && !credentials.identity.isEmpty();
    assert credentials.credential != null && !credentials.credential.isEmpty();
  }

  /**
   * Test of validateCredentials method, of class GceLauncher.
   *
   * @throws se.kth.karamel.common.exception.InvalidCredentialsException
   */
  @Test
  public void validateCredentials() throws InvalidCredentialsException {
    assert context != null;
  }

  @Test
  public void testForkMachines() throws InvalidCredentialsException, RunNodesException, URISyntaxException {
    int size = 8;
    List<MachineRuntime> machines = forkMachines(size, "europe-west1-b");
    assert machines.size() == size;
    for (MachineRuntime machine : machines) {
      assert machine.getId() != null && !machine.getId().isEmpty();
      assert machine.getName() != null && !machine.getName().isEmpty();
      assert machine.getPublicIp() != null && !machine.getPublicIp().isEmpty();
      assert machine.getPrivateIp() != null && !machine.getPrivateIp().isEmpty();
    }
  }

  @Test
  public void testCleanUp() throws InvalidCredentialsException, RunNodesException, URISyntaxException, KaramelException {
    int size = 8;
    String zone = "europe-west1-b";
    List<MachineRuntime> machines = forkMachines(size, zone);
    List<String> vms = new ArrayList<>(machines.size());
    for (MachineRuntime machine : machines) {
      vms.add(machine.getName());
    }
    GceLauncher launcher = new GceLauncher(context);
    launcher.cleanup(vms, zone);
    InstanceApi instanceApi = context.getGceApi().instancesInZone(zone);
    assert !instanceApi.list().hasNext();
  }

  @Test
  public void testCreateFirewall() throws KaramelException {
    GceLauncher launcher = new GceLauncher(context);
    String clusterName = "c1";
    String groupName = "g1";
    String p1 = "22";
    String p2 = "80";
    String pr = "tcp";
    launcher.createFirewall(clusterName, groupName, "10.240.0.0/16", ImmutableSet.of(p1 + "/" + pr, p2 + "/" + pr));
    String networkName = Settings.UNIQUE_GROUP_NAME("gce", clusterName, groupName);
    String fw1Name = Settings.UNIQUE_FIREWALL_NAME(networkName, p1, pr);
    String fw2Name = Settings.UNIQUE_FIREWALL_NAME(networkName, p2, pr);
    FirewallApi fwApi = context.getFireWallApi();
    Firewall fw = fwApi.get(fw1Name);
    assert fw != null && !fw.allowed().isEmpty() && !fw.allowed().get(0).ports().isEmpty() && fw.allowed().get(0).ports().get(0).equalsIgnoreCase(p1);
    fw = fwApi.get(fw2Name);
    assert fw != null && !fw.allowed().isEmpty() && !fw.allowed().get(0).ports().isEmpty() && fw.allowed().get(0).ports().get(0).equalsIgnoreCase(p2);
  }

  private List<MachineRuntime> forkMachines(int size, String zone) throws InvalidCredentialsException, RunNodesException {
    Gce gce = new Gce();
    gce.setImageName("centos-6-v20150423");
    gce.setImageType(Gce.ImageType.centos);
    gce.setZone(zone);
    gce.setMachineType(Gce.MachineType.n1_standard_1);
    GceLauncher launcher = new GceLauncher(context);
    List<MachineRuntime> machines = launcher.forkMachines("c1", "g1", size, gce);

    return machines;
  }
}
