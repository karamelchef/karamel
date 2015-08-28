package se.kth.karamel.backend.evaluation;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author Hooman
 */
public class KaramelEvaluation {

  private static final Logger logger = Logger.getLogger(KaramelEvaluation.class);
  public static final String NUM_VM_PARAM = "numvm";
  static KaramelApi api = new KaramelApiImpl();

  public static void main(String[] args) throws IOException, KaramelException, InterruptedException {
    String fileName = "flink_gce_eval";
    String clusterName = "flink";
    String outputName = "timestat.txt";
    int numRound = 5;
    int increase = 20;

    ClusterStatistics.setFileName(outputName);
    String ymlString = Resources.toString(
        Resources.getResource("se/kth/karamel/backend/evaluation/" + fileName + ".yml"), Charsets.UTF_8);
    SshKeyPair sshKeys = api.loadSshKeysIfExist("");
    if (sshKeys == null) {
      sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
    }
    api.registerSshKeys(sshKeys);
    api.updateGceCredentialsIfValid(Settings.KARAMEL_ROOT_PATH + "/gce-key.json");

    KaramelEvaluation evaluation = new KaramelEvaluation();

    for (int i = 9; i < 100; i = i + increase) {
      ymlString = ymlString.replace(NUM_VM_PARAM, String.valueOf(i));
      ClusterStatistics.setExperimentName(String.format("%s%d", fileName, i));
      for (int j = 0; j < numRound; j++) {
        evaluation.evaluatePhases(ymlString, clusterName);
      }
    }
  }

  private void evaluatePhases(String ymlString, String clusterName)
      throws IOException, KaramelException, InterruptedException {

    String json = api.yamlToJson(ymlString);
    api.startCluster(json);
    Thread.sleep(2000);
    ClusterRuntime clusterRuntime = ClusterService.getInstance().clusterStatus(clusterName);
    while (clusterRuntime.getPhase() != ClusterRuntime.ClusterPhases.NOT_STARTED || clusterRuntime.isFailed()) {

      if (clusterRuntime.getPhase() == ClusterRuntime.ClusterPhases.INSTALLED) {
        api.processCommand("purge " + clusterName);
      }
      logger.info(api.processCommand("status").getResult());
      Thread.sleep(30000);
    }
  }
}
