package se.kth.karamel.common.util.settings;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 2015-05-16.
 */
public enum KaramelTimeSetting {

  INSTALLATION_DAG_THREADPOOL_SIZE(100),
  SSH_CONNECT_RETRIES(5),
  SSH_CONNECT_INTERVALS(5 * 1000),
  SSH_PING_INTERVAL(10 * 1000),
  MACHINE_TASKRUNNER_BUSYWAITING_INTERVALS(100),
  CLUSTER_STATUS_CHECKING_INTERVAL(1000),
  CLUSTER_FAILURE_DETECTION_INTERVAL(5000),
  SSH_CONNECTION_TIMEOUT(24 * 3600 * 1000),
  SSH_SESSION_TIMEOUT(24 * 3600 * 1000);
  private static final Map<Integer, KaramelTimeSetting> lookup = new HashMap<>();

  static {
    for (KaramelTimeSetting s : EnumSet.allOf((KaramelTimeSetting.class))) {
      lookup.put(s.timeConstrain, s);
    }
  }

  private int timeConstrain;

  private KaramelTimeSetting(int timeConstrain) {
    this.timeConstrain = timeConstrain;
  }

  public static KaramelTimeSetting get(int code) {
    return lookup.get(code);
  }

  public int getTimeConstrain() {
    return timeConstrain;
  }
}
