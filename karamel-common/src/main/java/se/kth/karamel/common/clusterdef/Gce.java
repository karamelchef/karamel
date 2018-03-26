package se.kth.karamel.common.clusterdef;

import se.kth.karamel.common.util.GceSettings;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author hooman
 */
public class Gce extends Provider {

  private String type;
  private String zone;
  private String image;
  private String vpc;
  private Long diskSize;
  private String subnet;
  
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

  /**
   * @return the vpc network
   */
  public String getVpc() {
    return vpc;
  }

  /**
   * @param vpc the vpc network to set
   */
  public void setVpc(String vpc) {
    this.vpc = vpc;
  }
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
  
  /**
   * Size for the boot disk in GB
   * @return the diskSize in GB
   */
  public Long getDiskSize() {
    return diskSize;
  }
  
  /**
   * Boot Disksize in GB
   * @param diskSize
   */
  public void setDiskSize(Long diskSize) {
    this.diskSize = diskSize;
  }
  
  /**
   * Subnet name
   * @return the subnet
   */
  public String getSubnet() {
    return subnet;
  }
  
  /**
   * Subnet name
   * @param subnet
   */
  public void setSubnet(String subnet) {
    this.subnet = subnet;
  }
  
  @Override
  public Gce cloneMe() {
    Gce gce = new Gce();
    gce.setUsername(this.getUsername());
    gce.setImage(image);
    gce.setType(type);
    gce.setVpc(vpc);
    gce.setZone(zone);
    gce.setDiskSize(diskSize);
    gce.setSubnet(subnet);
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
      if(clone.getVpc() == null){
        clone.setVpc(parentGce.getVpc());
      }
      if(clone.getDiskSize() == null){
        clone.setDiskSize(parentGce.getDiskSize());
      }
      if(clone.getSubnet() == null){
        clone.setSubnet(parentGce.getSubnet());
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
    if(clone.getVpc() == null){
      clone.setVpc(GceSettings.DEFAULT_NETWORK_NAME);
    }
    if(clone.getDiskSize() == null){
      clone.setDiskSize(GceSettings.DEFAULT_DISKSIZE_IN_GB);
    }
    return clone;
  }

  @Override
  public void validate() throws ValidationException {
    // Currently nothing to validate. But IP range can be validate here.
  }
  
  @Override
  public String toString() {
    return "Gce{" +
        "type='" + type + '\'' +
        ", zone='" + zone + '\'' +
        ", image='" + image + '\'' +
        ", vpc='" + vpc + '\'' +
        ", diskSize=" + diskSize +
        ", subnet='" + subnet + '\'' +
        '}';
  }
}
