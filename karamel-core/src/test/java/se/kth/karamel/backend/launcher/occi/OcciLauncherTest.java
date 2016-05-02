package se.kth.karamel.backend.launcher.occi;

import cz.cesnet.cloud.occi.api.Client;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.NodeRunTime;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.util.OcciCredentials;

/**
 *
 * @author mtd
 */
public class OcciLauncherTest {

  static OcciContext context;

  public OcciLauncherTest() {
  }

  @Test
  public void dummyTest() {
    Assert.assertTrue(true);
  }

  /**
   * Test of validateCredentials method, of class OcciLauncher.
   */
//    @Test
  public void testValidateCredentials() throws Exception {
    System.out.println("validateCredentials");
    OcciCredentials occiCredentials = null;
    OcciContext expResult = null;
    OcciContext result = OcciLauncher.validateCredentials(occiCredentials);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of readCredentials method, of class OcciLauncher.
   */
//    @Test
  public void testReadCredentials() {
    System.out.println("readCredentials");
    Confs confs = null;
    OcciCredentials expResult = null;
    OcciCredentials result = OcciLauncher.readCredentials(confs);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of cleanup method, of class OcciLauncher.
   */
//    @Test
  public void testCleanup() throws Exception {
    System.out.println("cleanup");
    JsonCluster definition = null;
    ClusterRuntime runtime = null;
    OcciLauncher instance = null;
    instance.cleanup(definition, runtime);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of createCompute method, of class OcciLauncher.
   */
//    @Test
  public void testCreateCompute() throws Exception {
    System.out.println("createCompute");
    Client client = null;
    String vmName = "";
    String occiImage = "";
    String occiImageSize = "";
    String SSHPublicKey = "";
    OcciLauncher instance = null;
    URI expResult = null;
    URI result = instance.createCompute(client, vmName, occiImage, occiImageSize, SSHPublicKey);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getPublicIPs method, of class OcciLauncher.
   */
//    @Test
  public void testGetPublicIPs() throws Exception {
    System.out.println("getPublicIPs");
    Client client = null;
    URI location = null;
    OcciLauncher instance = null;
    ArrayList<String> expResult = null;
    ArrayList<String> result = instance.getPublicIPs(client, location);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of forkGroup method, of class OcciLauncher.
   */
//    @Test
  public void testForkGroup() throws Exception {
    System.out.println("forkGroup");
    JsonCluster definition = null;
    ClusterRuntime runtime = null;
    String groupName = "";
    OcciLauncher instance = null;
    String expResult = "";
    String result = instance.forkGroup(definition, runtime, groupName);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of forkMachines method, of class OcciLauncher.
   */
//    @Test
  public void testForkMachines() throws Exception {
    System.out.println("forkMachines");
    JsonCluster definition = null;
    ClusterRuntime runtime = null;
    String groupName = "";
    OcciLauncher instance = null;
    List<NodeRunTime> expResult = null;
    List<NodeRunTime> result = instance.forkMachines(definition, runtime, groupName);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
