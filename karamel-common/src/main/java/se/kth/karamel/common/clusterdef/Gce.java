package se.kth.karamel.common.clusterdef;

import se.kth.karamel.common.launcher.gce.GceSettings;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author hooman
 */
public class Gce extends Provider {

  private String type;
  // TODO: IP range to give to VMs.
//  private String network;
  private String zone;
  private String image;

  /**
   * Machine type.
   *
   * @return
   */
  public String getType() {
    return type;
  }

  /**
   * Machine type.
   *
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }

  public String getZone() {
    return zone;
  }

  public void setZone(String zone) {
    this.zone = zone;
  }

//  /**
//   * @return the network
//   */
//  public String getNetwork() {
//    return network;
//  }
//
//  /**
//   * @param network the network to set
//   */
//  public void setNetwork(String network) {
//    this.network = network;
//  }
  /**
   * Image name
   *
   * @return the image
   */
  public String getImage() {
    return image;
  }

  /**
   * Image name
   *
   * @param image the image to set
   */
  public void setImage(String image) {
    this.image = image;
  }

  @Override
  public Gce cloneMe() {
    Gce gce = new Gce();
    gce.setUsername(this.getUsername());
    gce.setImage(image);
    gce.setType(type);
//    gce.setNetwork(network);
    gce.setZone(zone);

    return gce;
  }

  @Override
  public Gce applyParentScope(Provider parentScopeProvider) {
    Gce clone = cloneMe();
    if (parentScopeProvider instanceof Gce) {
      Gce parentGce = (Gce) parentScopeProvider;
      if (clone.getUsername() == null) {
        clone.setUsername(parentGce.getUsername());
      }
      if (clone.getImage() == null) {
        clone.setImage(parentGce.getImage());
      }
      if (clone.getZone() == null) {
        clone.setZone(parentGce.getZone());
      }
      if (clone.getType() == null) {
        clone.setType(parentGce.getType());
      }
    }
    return clone;
  }

  @Override
  public Gce applyDefaults() {
    Gce clone = cloneMe();
    if (clone.getUsername() == null) {
      clone.setUsername(Settings.AWS_VM_USERNAME_DEFAULT);
    }
    if (clone.getImage() == null) {
      clone.setImage(GceSettings.DEFAULT_IMAGE);
    }
    if (clone.getZone() == null) {
      clone.setZone(GceSettings.DEFAULT_ZONE);
    }
    if (clone.getType() == null) {
      clone.setType(GceSettings.DEFAULT_MACHINE_TYPE);
    }
    return clone;
  }

  @Override
  public void validate() throws ValidationException {
    // Currently nothing to validate. But IP range can be validate here.
  }
}
