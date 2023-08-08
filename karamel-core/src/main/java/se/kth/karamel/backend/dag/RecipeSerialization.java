package se.kth.karamel.backend.dag;

import org.apache.log4j.Logger;
import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RecipeSerialization {
  private static final Logger logger = Logger.getLogger(RecipeSerialization.class);
  private final Semaphore parallelism;
  private final Integer maxParallelism;
  private final Set<RecipeSerializationClaim> claims;
  private final ReentrantReadWriteLock claimsLock;
  private boolean failed = false;

  public RecipeSerialization(Integer parallelism) {
    this.parallelism = new Semaphore(parallelism, true);
    this.maxParallelism = parallelism;
    claims = new TreeSet<>(new Comparator<RecipeSerializationClaim>() {
      @Override
      public int compare(RecipeSerializationClaim t0, RecipeSerializationClaim t1) {
        return t0.getClaimedAt().compareTo(t1.getClaimedAt());
      }
    });
    claimsLock = new ReentrantReadWriteLock(true);
  }

  public void release(RunRecipeTask task) {
    parallelism.release();
    claimsLock.writeLock().lock();
    try {
      claims.remove(new RecipeSerializationClaim(task));
    } finally {
      claimsLock.writeLock().unlock();
    }
    logger.info("Released serializable execution of " + task.getRecipeCanonicalName() + " on "
        + task.getMachineId());
  }

  public void prepareToExecute(RunRecipeTask task) throws InterruptedException {
    logger.info("Prepare to run " + task.getRecipeCanonicalName() + " on " + task.getMachineId());
    if (!parallelism.tryAcquire()) {
      logger.info("Could not run " + task.getRecipeCanonicalName() + " on " + task.getMachineId()
          + " at the moment because parallelism is limited. Available parallelism permits: "
          + parallelism.availablePermits() + "/" + maxParallelism + " - we wait until a permit becomes available."
          + " Current claims: " + printableClaims());
      task.blocked();
      parallelism.acquire();
    }
    claimsLock.writeLock().lock();
    try {
      claims.add(new RecipeSerializationClaim(task));
    } finally {
      claimsLock.writeLock().unlock();
    }
    logger.info("Proceed with running " + task.getRecipeCanonicalName() + " on " + task.getMachineId());
  }

  private String printableClaims() {
    claimsLock.readLock().lock();
    try {
      StringBuffer sb = new StringBuffer();
      Iterator<RecipeSerializationClaim> i = claims.iterator();
      int o = 0;
      while (i.hasNext()) {
        sb.append(String.format("[%d] ", o));
        sb.append(i.next().getId()).append(" ");
        o++;
      }
      return sb.toString();
    } finally {
      claimsLock.readLock().unlock();
    }
  }

  public synchronized void setFailedStatus(boolean failed) {
    this.failed = failed;
  }

  public synchronized boolean hasFailed() {
    return failed;
  }
}
