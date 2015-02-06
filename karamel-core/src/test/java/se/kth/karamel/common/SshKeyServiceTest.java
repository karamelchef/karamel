/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import org.junit.Test;

/**
 *
 * @author kamal
 */
public class SshKeyServiceTest {

//  @Test
  public void testNonexistenceConfs() {
    Confs confs = Confs.loadAllConfsForCluster("HopsHup");
    System.out.println(confs.getProperty(Settings.SSH_PRIKEY_PATH_KEY));
  }

//  @Test
  public void testGenerateKeys() {
    SshKeyService.generateAndStoreSshKeys("HopsHup");
  }
}
