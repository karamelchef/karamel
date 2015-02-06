/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;

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

  public void writeClusterConfs(String clusterName) {
    FileOutputStream out = null;
    try {
      File folder = new File(Settings.CLUSTER_CLUSTER_FOLDER(clusterName));
      if (!folder.exists()) {
        folder.mkdirs();
      }
      File file = new File(folder, Settings.KARAMEL_CONF_NAME);
      out = new FileOutputStream(file);
      store(out, clusterName);
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
    return applyDefaults(prop);
  }

  public static Confs loadConfs() {
    Confs prop = new Confs();
    return applyDefaults(prop);
  }

  public static Confs applyDefaults(Confs prop) {
    String pubKeyPath = prop.getProperty(Settings.SSH_PUBKEY_PATH_KEY);
    String priKeyPath = prop.getProperty(Settings.SSH_PRIKEY_PATH_KEY);
    if ((pubKeyPath == null || priKeyPath == null)) {
      if (Settings.DEFAULT_PRIKEY_PATH != null) {
        pubKeyPath = Settings.DEFAULT_PUBKEY_PATH;
        priKeyPath = Settings.DEFAULT_PRIKEY_PATH;
        prop.put(Settings.SSH_PUBKEY_PATH_KEY, pubKeyPath);
        prop.put(Settings.SSH_PRIKEY_PATH_KEY, priKeyPath);
      }
    }
    return prop;
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
