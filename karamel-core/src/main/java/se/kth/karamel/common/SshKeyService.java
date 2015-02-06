/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.jclouds.ssh.SshKeys;
import se.kth.karamel.common.exception.SshKeysNotfoundException;

/**
 *
 * @author kamal
 */
public class SshKeyService {

  private static final Logger logger = Logger.getLogger(SshKeyService.class);

  public static SshKeyPair generateAndStoreSshKeys(String clusterName) {
    Map<String, String> keys = SshKeys.generate();
    String pub = keys.get("public");
    String pri = keys.get("private");
    File folder = new File(Settings.CLUSTER_SSH_FOLDER(clusterName));
    if (!folder.exists()) {
      folder.mkdirs();
    }
    File pubFile = new File(folder, Settings.CLUSTER_PUBKEY_FILENAME);

    try {
      FileOutputStream pubOut = new FileOutputStream(pubFile);
      Writer out = new OutputStreamWriter(pubOut, "UTF8");
      out.write(pub);
      out.close();
    } catch (IOException ex) {
      logger.error("", ex);
    }

    File priFile = new File(folder, Settings.CLUSTER_PRIKEY_FILENAME);

    try {
      FileOutputStream priOut = new FileOutputStream(priFile);
      Writer out = new OutputStreamWriter(priOut, "UTF8");
      out.write(pri);
      out.close();
    } catch (IOException ex) {
      logger.error("", ex);
    }

    SshKeyPair keyPair = new SshKeyPair();
    keyPair.setPrivateKey(pri);
    keyPair.setPublicKey(pub);
    keyPair.setPrivateKeyPath(folder + File.separator + Settings.CLUSTER_PRIKEY_FILENAME);
    keyPair.setPublicKeyPath(folder + File.separator + Settings.CLUSTER_PUBKEY_FILENAME);
    return keyPair;
  }

  public static SshKeyPair loadSshKeys(Confs confs) throws SshKeysNotfoundException {
    String pubkeyPath = confs.getProperty(Settings.SSH_PUBKEY_PATH_KEY);
    String prikeyPath = confs.getProperty(Settings.SSH_PRIKEY_PATH_KEY);
    return loadSshKeys(pubkeyPath, prikeyPath);
  }

  public static SshKeyPair loadSshKeys(String pubkeyPath, String prikeyPath) throws SshKeysNotfoundException {
    String pubKey = null;
    String priKey = null;
    try {
      BufferedReader r1;
      r1 = new BufferedReader(new FileReader(new File(pubkeyPath)));
      pubKey = r1.readLine();
      r1.close();

      try (Scanner scanner = new Scanner(new File(prikeyPath))) {
        priKey = scanner.useDelimiter("\\A").next();
      }

    } catch (IOException ex) {
      throw new SshKeysNotfoundException(String.format("Unsuccessful to load ssh keys from '%s' and/or '%s'", pubkeyPath, prikeyPath), ex);
    }
    if (pubKey != null && priKey != null) {
      SshKeyPair keypair = new SshKeyPair();
      keypair.setPublicKeyPath(pubkeyPath);
      keypair.setPublicKey(pubKey);
      keypair.setPrivateKeyPath(prikeyPath);
      keypair.setPrivateKey(priKey);
      return keypair;
    }
    throw new SshKeysNotfoundException(String.format("Unsuccessful to load ssh keys from '%s' and/or '%s'", pubkeyPath, prikeyPath));

  }

}
