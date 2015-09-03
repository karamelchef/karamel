/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.backend.kandy;

import org.junit.Test;
import se.kth.karamel.backend.stats.ClusterStats;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class KandyRestClientTest {
 
  @Test
  public void testPushStats() throws KaramelException {
    ClusterStats stats = new ClusterStats("test-cluster");
    KandyRestClient.pushClusterStats(stats);
  }
}
