package se.kth.karamel.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcOutputConsumer implements Callable<String> {

  private static final Logger LOGGER = Logger.getLogger(ProcOutputConsumer.class.toString());

  private InputStream in;

  public ProcOutputConsumer(InputStream in) {
    this.in = in;
  }

  @Override
  public String call() throws Exception {

    StringBuilder outputBuilder = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(in));

    char[] charArray = new char[1000];
    try {
      int actualBuffered = 0;
      while ((actualBuffered = br.read(charArray, 0, 1000)) != -1) {
        outputBuilder.append(charArray, 0, actualBuffered);
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not read the process output", e);
    } finally {
      try {
        br.close();
      } catch (IOException e) {
      }
    }
    return outputBuilder.toString();
  }
}
