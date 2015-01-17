/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

/**
 *
 * @author kamal
 * @param <K>
 * @param <V>
 */
public class Confs<K extends String, V extends String> extends Properties {


  public synchronized <K, V> void set(K k, V v) {
    if (v == null || v.toString().isEmpty()) {
      if (contains(k)) {
        remove(k);
      }
    } else {
      super.put(k, v);
    }
  }

  public static Confs loadEc2Confs() {
    return loadConfs(Settings.EC2_CONF_PATH);
  }

  public static Confs loadVagrantConfs() {
    return loadConfs(Settings.VAGRANT_CONF_PATH);
  }

  public static Confs loadConfs(String fileName) {
    Confs prop = new Confs();
    try {
      FileInputStream fis = new FileInputStream(new File(fileName));
      prop.load(fis);
    } catch (FileNotFoundException e) {
    } catch (IOException e) {
      e.printStackTrace();
    }
    return loadConfs(prop);
  }

  public static Confs loadConfs() {
    Confs prop = new Confs();
    return loadConfs(prop);
  }
  public static Confs loadConfs(Confs prop) {
    String pubKeyPath = prop.getProperty(Settings.SSH_PUBKEY_PATH_KEY, Settings.DEFAULT_PUBKEY_PATH);
    String priKeyPath = prop.getProperty(Settings.SSH_PRIKEY_PATH_KEY, Settings.DEFAULT_PRIKEY_PATH);
    loadConfs(pubKeyPath, priKeyPath, prop);
    return prop;
  }

  public static Properties loadConfs(String pubkeyPath, String prikeyPath, Properties prop) {
    try {
      BufferedReader r1;
      r1 = new BufferedReader(new FileReader(new File(pubkeyPath)));
      String pubKey = r1.readLine();
      r1.close();
      prop.put(Settings.SSH_PUBKEY_KEY, pubKey);
      String priKey;
      try (Scanner scanner = new Scanner(new File(prikeyPath))) {
        priKey = scanner.useDelimiter("\\A").next();
      }
      prop.put(Settings.SSH_PRIKEY_KEY, priKey);
    } catch (FileNotFoundException ex) {
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return prop;
  }

}
