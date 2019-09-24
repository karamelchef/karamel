package se.kth.karamel.backend.launcher.azure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.launcher.Launcher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.SshKeyPair;

public final class AzureLauncher extends Launcher {

  // Provider to use
  private static final String provider = "azurecompute-arm";

  // Required properties for Azure provider
  public static final String PROPERTY_AZURE_TENANT_ID = "azurecompute-arm.tenantId";
  public static final String PROPERTY_AZURE_SUBSCRIPTION_ID = "azurecompute-arm.subscriptionId";

  private static final Logger logger = Logger.getLogger(AzureLauncher.class);
  public static boolean TESTING = true;
//  public final Ec2Context context;
  public final SshKeyPair sshKeyPair;

  Set<String> keys = new HashSet<>();

  public AzureLauncher() {
    this.sshKeyPair = null;
  }

  @Override
  public void cleanup(JsonCluster definition, ClusterRuntime runtime) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String forkGroup(JsonCluster definition, ClusterRuntime runtime, String name) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<MachineRuntime> forkMachines(JsonCluster definition, ClusterRuntime runtime, String name) throws
      KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
