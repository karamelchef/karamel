package se.kth.karamel.client.model;

import se.kth.karamel.common.GceSettings;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author hooman
 */
public class Gce extends Provider {

  private String machineType;
  // TODO: IP range to give to VMs.
//  private String network;
  private String zone;
  private String imageName;

  public String getMachineType() {
    return machineType;
  }

  public void setMachineType(String type) {
    this.machineType = type;
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
   * @return the imageName
   */
  public String getImageName() {
    return imageName;
  }

  /**
   * @param imageName the imageName to set
   */
  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  @Override
  public Gce cloneMe() {
    Gce gce = new Gce();
    gce.setUsername(this.getUsername());
    gce.setImageName(imageName);
    gce.setMachineType(machineType);
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
      if (clone.getImageName() == null) {
        clone.setImageName(parentGce.getImageName());
      }
      if (clone.getZone() == null) {
        clone.setZone(parentGce.getZone());
      }
      if (clone.getMachineType() == null) {
        clone.setMachineType(parentGce.getMachineType());
      }
    }
    return clone;
  }

  @Override
  public Gce applyDefaults() {
    Gce clone = cloneMe();
    if (clone.getUsername() == null) {
      clone.setUsername(Settings.PROVIDER_EC2_DEFAULT_USERNAME);
    }
    if (clone.getImageName() == null) {
      clone.setImageName(GceSettings.DEFAULT_IMAGE);
    }
    if (clone.getZone() == null) {
      clone.setZone(GceSettings.DEFAULT_ZONE);
    }
    if (clone.getMachineType() == null) {
      clone.setMachineType(GceSettings.DEFAULT_MACHINE_TYPE);
    }
    return clone;
  }

  @Override
  public void validate() throws ValidationException {
    // Currently nothing to validate. But IP range can be validate here.
  }
}
