/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.machines;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.PTYMode;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.SessionChannel;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class SshShell {

  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SshShell.class);
  private final String privateKey;
  private final String publicKey;
  private final String ipAddress;
  private final String sshUser;
  private final int sshPort;
  private SSHClient client = null;
  private SessionChannel shell = null;
  private Session session = null;
  private InputStreamReader stdAll;
  private SequenceInputStream sequenceInputStream;
  private final Semaphore semaphor = new Semaphore(1);
  private StringBuilder builder = new StringBuilder();
  private Thread streamReader;

  public SshShell(String privateKey, String publicKey, String ipAddress, String sshUser, int sshPort) {
    this.privateKey = privateKey;
    this.publicKey = publicKey;
    this.ipAddress = ipAddress;
    this.sshUser = sshUser;
    this.sshPort = sshPort;
  }

  public void connect() throws KaramelException {
    try {
      if (isConnected()) {
        disconnect();
      }

      client = new SSHClient();
      client.addHostKeyVerifier(new PromiscuousVerifier());

      KeyProvider keys = client.loadKeys(privateKey, publicKey, null);
      client.connect(ipAddress, sshPort);
      client.authPublickey(sshUser, keys);

      session = client.startSession();

      Map<PTYMode, Integer> modes = new HashMap<>();
      session.allocatePTY("vt220", 80, 24, 0, 0, modes);

      shell = (SessionChannel) session.startShell();
      sequenceInputStream = new SequenceInputStream(shell.getInputStream(), shell.getErrorStream());
      stdAll = new InputStreamReader(sequenceInputStream);

      streamReader = new Thread() {

        @Override
        public void run() {
          int c;
          try {
            while ((c = stdAll.read()) != -1) {
              semaphor.acquire();
              builder.append((char) c);
              semaphor.release();
            }
          } catch (IOException | InterruptedException ex) {
            logger.error("", ex);
          }
        }

      };
      streamReader.start();
    } catch (Exception ex) {
      logger.error("", ex);
      throw new KaramelException("Exception Occured", ex);
    }
  }

  public void exec(String cmdStr) throws KaramelException {
    try {
      byte[] bytes = cmdStr.getBytes();
      shell.getOutputStream().write(bytes);
      shell.getOutputStream().flush();
    } catch (Exception ex) {
      logger.error("", ex);
      throw new KaramelException("", ex);
    }
  }

  public String readStreams() throws KaramelException {
    try {
      semaphor.acquire();
      String s = builder.toString();
      builder = new StringBuilder();
      semaphor.release();
//      if (s != null && !s.isEmpty()) {
//        s = s.replace("\r", "\\r").replaceAll("\n", "\\n").trim();
//      }
      logger.info("shell output:\n" + s + "\n");
      return s;
    } catch (Exception ex) {
      logger.error("", ex);
      throw new KaramelException("Exception occured", ex);
    }

  }

  public boolean isConnected() throws KaramelException {
    try {
      return client != null && client.isConnected() && session != null && session.isOpen() && shell != null && shell.isOpen();
    } catch (Exception ex) {
      logger.error("", ex);
      throw new KaramelException("Exception occured", ex);
    }
  }

  public void disconnect() throws KaramelException {
    try {
      if (streamReader != null && streamReader.isAlive()) {
        streamReader.interrupt();
      }
      session.close();
      client.disconnect();
    } catch (IOException ex) {
      logger.error("", ex);
      throw new KaramelException("Exception occured", ex);
    }
  }
}
