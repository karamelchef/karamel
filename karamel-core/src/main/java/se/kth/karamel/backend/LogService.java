/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.common.FilesystemUtil;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class LogService {

  private static final Logger logger = Logger.getLogger(LogService.class);

  public static void cleanup(String clusterName) {
    logger.info(String.format("Trashing old logs of '%s'", clusterName));
    String path = Settings.CLUSTER_LOG_FOLDER(clusterName);
    try {
      FilesystemUtil.deleteRecursive(path);
    } catch (FileNotFoundException ex) {
    }
  }

  public static void serializeTaskLogs(Task task, String machineIp, InputStream out, InputStream err) {
    String publicIp = task.getMachine().getPublicIp();
    String clusterName = task.getMachine().getGroup().getCluster().getName();
    String errFilePath = Settings.TASK_ERROR_FILE_PATH(clusterName, publicIp, task.getName());
    String outFilePath = Settings.TASK_ERROR_FILE_PATH(clusterName, publicIp, task.getName());

    File folder = new File(Settings.MACHINE_LOG_FOLDER(clusterName, machineIp));

    if (!folder.exists()) {
      folder.mkdirs();
    }
    appendToFile(errFilePath, err);
    appendToFile(outFilePath, out);
  }

  private static void appendToFile(String filePath, InputStream in) {
    try {
      File file = new File(filePath);
      file.createNewFile();
      try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
        final byte[] buffer = new byte[65536];
        int r;
        while ((r = in.read(buffer)) > 0) {
          String decoded = new String(buffer, "UTF-8");
          pw.print(decoded);
        }

      } catch (IOException e) {
        logger.error("", e);
      }
    } catch (IOException e) {
      logger.error("", e);
    }
  }

  public static String loadErrLog(String clusterName, String publicIp, String taskName) throws KaramelException {
    String errFilPath = Settings.TASK_ERROR_FILE_PATH(clusterName, publicIp, taskName);
    return loadLogFile(errFilPath);
  }

  public static String loadOutLog(String clusterName, String publicIp, String taskName) throws KaramelException {
    String outFilPath = Settings.TASK_OUTPUT_FILE_PATH(clusterName, publicIp, taskName);
    return loadLogFile(outFilPath);
  }

  public static String loadLogFile(String filePath) throws KaramelException {
    byte[] encoded;
    try {
      encoded = Files.readAllBytes(Paths.get(filePath));
      return new String(encoded, "UTF-8");
    } catch (IOException ex) {
      throw new KaramelException(String.format("Couldn't read the log file '%s'", filePath), ex);
    }
  }
}
