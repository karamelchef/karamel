package se.kth.karamel.backend.dag;

import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;

import java.time.Instant;
import java.util.Objects;

public class RecipeSerializationClaim {
  private final String id;
  private final Instant claimedAt;

  public static String serializationClaimId(RunRecipeTask task) {
    return String.format("%s@%s", task.getRecipeCanonicalName(), task.getMachineId());
  }

  public RecipeSerializationClaim(RunRecipeTask task) {
    id = serializationClaimId(task);
    claimedAt = Instant.now();
  }

  public String getId() {
    return id;
  }

  public Instant getClaimedAt() {
    return claimedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RecipeSerializationClaim that = (RecipeSerializationClaim) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
