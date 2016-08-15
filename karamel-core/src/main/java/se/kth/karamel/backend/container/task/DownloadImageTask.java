package se.kth.karamel.backend.container.task;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.AuthConfig;
import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * Created by shelan on 5/26/16.
 */
public class DownloadImageTask implements Runnable {
  private DockerClient client;
  private CountDownLatch countDownLatch;
  private static final Logger logger = Logger.getLogger(DownloadImageTask.class);

  public DownloadImageTask(DockerClient client, CountDownLatch countDownLatch) {
    this.client = client;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    try {
      client.pull("shelan/karamel-node:v4.0.0", AuthConfig.builder().build());
    } catch (DockerException e) {
      logger.error("Error while downloading docker container", e);
    } catch (InterruptedException e) {
      logger.error("Interrupted while downloading docker container", e);
    }
    finally {
      countDownLatch.countDown();
    }
  }
}
