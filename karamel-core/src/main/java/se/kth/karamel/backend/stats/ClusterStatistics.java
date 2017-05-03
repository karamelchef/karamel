package se.kth.karamel.backend.stats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author Hooman
 */
public class ClusterStatistics {

  public static final String DEFAULT_TIME_STAT_FILE_DIR = Settings.KARAMEL_ROOT_PATH;
  private static String fileName = null;
  private static FileWriter writer = null;
  private static String experimentName = "";
  private final static Map<String, Long> timeStats = new HashMap<>();
  private static final Logger logger = Logger.getLogger(ClusterStatistics.class);
  private static long startTime = 0;

  public static void addTimeStat(String key, long time) {
    timeStats.put(key, time);
    logger.debug(String.format("%s time: %d", key, time));
    try {
      if (fileName != null) {
        if (writer == null) {
          writer = new FileWriter(new File(DEFAULT_TIME_STAT_FILE_DIR + "/" + fileName), true);
        }
      }
      writer.append(String.format("%s,%s,%d\n", experimentName, key, time));
      writer.flush();
    } catch (IOException ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  public static void startTimer() {
    startTime = System.currentTimeMillis();
  }

  public static long stopTimer() {
    return System.currentTimeMillis() - startTime;
  }

  /**
   * @param aFileName the fileName to set
   */
  public static void setFileName(String aFileName) {
    fileName = aFileName;
  }

  /**
   * @param aExperimentName the experimentName to set
   */
  public static void setExperimentName(String aExperimentName) {
    experimentName = aExperimentName;
  }

}
