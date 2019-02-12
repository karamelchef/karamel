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
  private Boolean preemptible;
  private Integer nvme;
  private Integer hdd;
  private Integer ssd;
  
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

  /**
   * Is this a preemptible VM or not?
   * @return true (if preemptible)
   */
  public Boolean isPreemptible() {
    return preemptible;
  }
  
  public Boolean getPreemptible() {
    return preemptible;
  }

  /** 
   * Set 'true' to make the VM preemptible
   * @param preemptible 'true' for preemptible, otherise 'false'
   */
  public void setPreemptible(Boolean preemptible) {
    this.preemptible = preemptible;
  }
  
  /**
   * Number of NVMe disks (maximum 8)
   * @return the number of NVMe disks.
   */
  public Integer getNvme() {
    return nvme;
  }
  
  /**
   * Number of NVMe disks (maximum 8)
   * @param nvme
   */
  public void setNvme(Integer nvme) {
    this.nvme = nvme;
  }
  
  /**
   * Number of HDD disks.
   * @return
   */
  public Integer getHdd() {
    return hdd;
  }
  
  /**
   * Number of HDD disks.
   * @param hdd
   */
  public void setHdd(Integer hdd) {
    this.hdd = hdd;
  }
  
  /**
   * Number of SSD disks.
   * @return
   */
  public Integer getSsd() {
    return ssd;
  }
  
  /**
   * Number of SSD disks.
   * @param ssd
   */
  public void setSsd(Integer ssd) {
    this.ssd = ssd;
  }
  
  @Override
  public Gce cloneMe() {
    Gce gce = new Gce();
    gce.setUsername(this.getUsername());
    gce.setImage(this.image);
    gce.setType(this.type);
    gce.setVpc(this.vpc);
    gce.setZone(this.zone);
    gce.setDiskSize(this.diskSize);
    gce.setSubnet(this.subnet);
    gce.setPreemptible(this.preemptible);
    gce.setNvme(this.nvme);
    gce.setHdd(this.hdd);
    gce.setSsd(this.ssd);
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
      if(clone.isPreemptible() == null){
        clone.setPreemptible(parentGce.isPreemptible());
      }
      if(clone.getNvme() == null){
        clone.setNvme(parentGce.getNvme());
      }
      if(clone.getHdd() == null){
        clone.setHdd(parentGce.getHdd());
      }
      if(clone.getSsd() == null){
        clone.setSsd(parentGce.getSsd());
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
    if(clone.isPreemptible()== null){
      clone.setPreemptible(GceSettings.DEFAULT_IS_PRE_EMPTIBLE);
    }
    if(clone.getNvme() == null){
      clone.setNvme(GceSettings.DEFAULT_NUMBER_NVMe_DISKS);
    }
    if(clone.getHdd() == null){
      clone.setHdd(GceSettings.DEFAULT_NUMBER_HDD_DISKS);
    }
    if(clone.getSsd() == null){
      clone.setSsd(GceSettings.DEFAULT_NUMBER_SSD_DISKS);
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
        ", preemptible='" + preemptible + '\'' +
        ", nvme=" + nvme +
        ", hdd=" + hdd +
        ", ssd=" + ssd +
        '}';
  }
}
