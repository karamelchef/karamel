/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.jclouds.ssh.SshKeys;
import se.kth.karamel.common.exception.SshKeysNotfoundException;

/**
 *
 * @author kamal
 * @param <K>
 * @param <V>
 */
public class Confs<K extends String, V extends String> extends Properties {

  private static final Logger logger = Logger.getLogger(Confs.class);

  public synchronized <K, V> void set(K k, V v) {
    if (v == null || v.toString().isEmpty()) {
      if (contains(k)) {
        remove(k);
      }
    } else {
      super.put(k, v);
    }
  }

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

  public void writeClusterConfs(String clusterName) {
    Confs clone = clone();
    clone.remove(Settings.SSH_PUBKEY_KEY);
    clone.remove(Settings.SSH_PRIKEY_KEY);
    FileOutputStream out = null;
    try {
      File folder = new File(Settings.CLUSTER_CLUSTER_FOLDER(clusterName));
      if (!folder.exists()) {
        folder.mkdirs();
      }
      File file = new File(folder, Settings.KARAMEL_CONF_NAME);
      out = new FileOutputStream(file);
      clone.store(out, clusterName);
    } catch (IOException ex) {
      logger.error("", ex);
    } finally {
      try {
        out.close();
      } catch (IOException ex) {
        logger.error("", ex);
      }
    }
  }

  public static Confs loadJustClusterConfs(String clusterName) {
    Confs clusterConf = loadConfs(Settings.CLUSTER_CLUSTER_FOLDER(clusterName));
    return clusterConf;
  }

  public static Confs loadAllConfsForCluster(String clusterName) {
    Confs karamelConf = loadKaramelConfs();
    Confs clusterConf = loadConfs(Settings.CLUSTER_CLUSTER_FOLDER(clusterName));
    for (String prop : clusterConf.stringPropertyNames()) {
      karamelConf.put(prop, clusterConf.getProperty(prop));
    }
    return karamelConf;
  }

  public static Confs loadKaramelConfs() {
    return loadConfs(Settings.KARAMEL_ROOT_PATH);
  }

  public static Confs loadConfs(String folder) {
    Confs prop = new Confs();
    try {
      File folders = new File(folder);
      File file = new File(folders, Settings.KARAMEL_CONF_NAME);
      if (!folders.exists()) {
        logger.info(String.format("Created empty conf file cus didn't exist. %s'", folder));
        folders.mkdirs();
      }
      FileInputStream fis = new FileInputStream(file);
      prop.load(fis);
    } catch (IOException e) {
      logger.warn(String.format("Couldn't find karamel conf file in '%s'", folder));
    }
    return loadConfs(prop);
  }

  public static Confs loadConfs() {
    Confs prop = new Confs();
    return loadConfs(prop);
  }

  public static Confs loadConfs(Confs prop) {
    String pubKeyPath = prop.getProperty(Settings.SSH_PUBKEY_PATH_KEY);
    String priKeyPath = prop.getProperty(Settings.SSH_PRIKEY_PATH_KEY);
    if ((pubKeyPath == null || priKeyPath == null)) {
      if (Settings.DEFAULT_PRIKEY_PATH != null) {
        pubKeyPath = Settings.DEFAULT_PUBKEY_PATH;
        priKeyPath = Settings.DEFAULT_PRIKEY_PATH;
        prop.put(Settings.SSH_PUBKEY_PATH_KEY, pubKeyPath);
        prop.put(Settings.SSH_PRIKEY_PATH_KEY, priKeyPath);
        loadConfs(pubKeyPath, priKeyPath, prop);
      }
    }
    return prop;
  }

  public static Properties loadConfs(String pubkeyPath, String prikeyPath, Properties prop) {
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
      logger.warn(String.format("Unsuccessful to load ssh keys from '%s' and/or '%s'", pubkeyPath, prikeyPath));
    }
    if (pubKey != null && priKey != null) {
      prop.put(Settings.SSH_PUBKEY_KEY, pubKey);
      prop.put(Settings.SSH_PRIKEY_KEY, priKey);
    }
    return prop;
  }

  public SshKeyPair getSshKeys() throws SshKeysNotfoundException {
    String pubkeyPath = getProperty(Settings.SSH_PUBKEY_PATH_KEY);
    String prikeyPath = getProperty(Settings.SSH_PRIKEY_PATH_KEY);
    String pubkey = getProperty(Settings.SSH_PUBKEY_KEY);
    String prikey = getProperty(Settings.SSH_PRIKEY_KEY);
    if (pubkey != null && prikey != null) {
      SshKeyPair keypair = new SshKeyPair();
      keypair.setPrivateKey(prikey);
      keypair.setPrivateKeyPath(prikeyPath);
      keypair.setPublicKey(pubkey);
      keypair.setPublicKeyPath(pubkeyPath);
      return keypair;
    } else {
      throw new SshKeysNotfoundException();
    }
  }

  @Override
  public synchronized Confs clone() {
    Confs clone = new Confs();
    for (String prop : stringPropertyNames()) {
      clone.put(prop, getProperty(prop));
    }
    return clone;
  }

}
