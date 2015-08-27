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
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jclouds.ssh.SshKeys;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.SshKeysNotfoundException;

/**
 *
 * @author kamal
 */
public class SshKeyService {

  private static final Logger logger = Logger.getLogger(SshKeyService.class);

  public static SshKeyPair generateAndStoreSshKeys() throws KaramelException {
    File folder = new File(Settings.KARAMEL_SSH_PATH);
    return generateAndStoreSshKeys(folder);
  }

  public static SshKeyPair generateAndStoreSshKeys(String clusterName) throws KaramelException {
    File folder = new File(Settings.CLUSTER_SSH_PATH(clusterName));
    return generateAndStoreSshKeys(folder);
  }

  public static SshKeyPair generateAndStoreSshKeys(File folder) throws KaramelException {
    if (!folder.exists()) {
      folder.mkdirs();
    }
    File pubFile = new File(folder, Settings.SSH_PUBKEY_FILENAME);
    File priFile = new File(folder, Settings.SSH_PRIVKEY_FILENAME);

    Map<String, String> keys = SshKeys.generate();
    String pub = keys.get("public");
    String pri = keys.get("private");

    try {
      FileOutputStream pubOut = new FileOutputStream(pubFile);
      Writer out = new OutputStreamWriter(pubOut, "UTF8");
      out.write(pub);
      out.close();
    } catch (IOException ex) {
      logger.error("", ex);
    }

    try {
      FileOutputStream priOut = new FileOutputStream(priFile);
      Writer out = new OutputStreamWriter(priOut, "UTF8");
      out.write(pri);
      out.close();
    } catch (IOException ex) {
      logger.error("", ex);
    }
    
    if (System.getProperty("os.name").toLowerCase().indexOf("win") == -1) {
      // public key permissions 644 (-rw-r--r--), private key 700 (drwx------)
      Set<PosixFilePermission> privatePerms = new HashSet<>();
      privatePerms.add(PosixFilePermission.OWNER_READ);
      privatePerms.add(PosixFilePermission.OWNER_WRITE);
      privatePerms.add(PosixFilePermission.OWNER_EXECUTE);
      privatePerms.remove(PosixFilePermission.GROUP_READ);
      privatePerms.remove(PosixFilePermission.GROUP_WRITE);
      privatePerms.remove(PosixFilePermission.GROUP_EXECUTE);
      privatePerms.remove(PosixFilePermission.OTHERS_READ);
      privatePerms.remove(PosixFilePermission.OTHERS_WRITE);
      privatePerms.remove(PosixFilePermission.OTHERS_EXECUTE);
      try {
        Files.setPosixFilePermissions(priFile.toPath(), privatePerms);
      } catch (IOException ex) {
        logger.error("Failed to set posix permissions on  generated private ssh-key. ", ex);
        throw new KaramelException(ex);
      }
      Set<PosixFilePermission> publicPerms = new HashSet<>();
      publicPerms.add(PosixFilePermission.OWNER_READ);
      publicPerms.add(PosixFilePermission.OWNER_WRITE);
      publicPerms.remove(PosixFilePermission.OWNER_EXECUTE);
      publicPerms.add(PosixFilePermission.GROUP_READ);
      publicPerms.remove(PosixFilePermission.GROUP_EXECUTE);
      publicPerms.remove(PosixFilePermission.GROUP_WRITE);
      publicPerms.add(PosixFilePermission.OTHERS_READ);
      publicPerms.remove(PosixFilePermission.OTHERS_EXECUTE);
      publicPerms.remove(PosixFilePermission.OTHERS_WRITE);
      try {
        Files.setPosixFilePermissions(pubFile.toPath(), publicPerms);
      } catch (IOException ex) {
        logger.error("Failed to set posix permissions on generated public ssh-key. ", ex);
        throw new KaramelException(ex);
      }
    }
    

    SshKeyPair keyPair = new SshKeyPair();
    keyPair.setPrivateKey(pri);
    keyPair.setPublicKey(pub);
    keyPair.setPrivateKeyPath(folder + File.separator + Settings.SSH_PRIVKEY_FILENAME);
    keyPair.setPublicKeyPath(folder + File.separator + Settings.SSH_PUBKEY_FILENAME);
    return keyPair;
  }

  public static SshKeyPair loadSshKeys(Confs confs) throws SshKeysNotfoundException {
    String pubkeyPath = confs.getProperty(Settings.SSH_PUBKEY_PATH_KEY);
    String privKeyPath = confs.getProperty(Settings.SSH_PRIVKEY_PATH_KEY);
    return loadSshKeys(pubkeyPath, privKeyPath, "");
  }

  public static SshKeyPair loadSshKeys(String pubkeyPath, String prikeyPath, String passphrase)
      throws SshKeysNotfoundException {
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
      throw new SshKeysNotfoundException(String.format("Unsuccessful to load ssh keys from '%s' and/or '%s'",
          pubkeyPath, prikeyPath), ex);
    }
    if (pubKey != null && priKey != null) {
      SshKeyPair keypair = new SshKeyPair();
      keypair.setPublicKeyPath(pubkeyPath);
      keypair.setPublicKey(pubKey);
      keypair.setPrivateKeyPath(prikeyPath);
      keypair.setPrivateKey(priKey);
      keypair.setPassphrase(passphrase);
      return keypair;
    }
    throw new SshKeysNotfoundException(String.format("Unsuccessful to load ssh keys from '%s' and/or '%s'",
        pubkeyPath, prikeyPath));

  }

}
