/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.honeytap.rules;

import org.junit.Test;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class RuleLoaderTest {
  
  @Test
  public void testRules() throws KaramelException {
    RuleLoader.getRulesOfGroup("honeytap", "worker");
  }
}
