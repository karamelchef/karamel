package se.kth.karamel.client.model;

import java.net.URI;
import java.net.URISyntaxException;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author hooman
 */
public class Gce extends Provider {

  public static final String DEFAULT_NETWORK_NAME = "default";

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

  public enum MachineType {

    n1_standard_1("n1-standard-1"),
    n1_standard_2("n1-standard-2"),
    n1_standard_4("n1-standard-4"),
    n1_standard_8("n1-standard-8"),
    n1_standard_16("n1-standard-16"),
    n1_standard_32("n1-standard-32");

    private final String type;

    private MachineType(String t) {
      type = t;
    }

    @Override
    public String toString() {
      return type;
    }

    public boolean equalsName(String otherType) {
      return (otherType == null) ? false : type.equals(otherType);
    }
  }

  public enum ImageType {

    debian("debian-cloud"),
    centos("centos-cloud"),
    ceros("ceros-cloud"),
    opensuse("opensuse-cloud"),
    ubuntu("ubuntu-os-cloud"),
    redhat("rhel-cloud"),
    suse("suse-cloud");
    private final String type;

    private ImageType(String t) {
      type = t;
    }

    @Override
    public String toString() {
      return type;
    }

    public boolean equalsName(String otherType) {
      return (otherType == null) ? false : type.equals(otherType);
    }
  }

  private MachineType machineType;
  private String network;
  private String zone;
  private ImageType imageType;
  private String imageName;

  public MachineType getMachineType() {
    return machineType;
  }

  public void setMachineType(MachineType type) {
    this.machineType = type;
  }

  public String getZone() {
    return zone;
  }

  public void setZone(String zone) {
    this.zone = zone;
  }

  public ImageType getImageType() {
    return imageType;
  }

  public void setImageType(ImageType image) {
    this.imageType = image;
  }

  /**
   * @return the network
   */
  public String getNetwork() {
    return network;
  }

  /**
   * @param network the network to set
   */
  public void setNetwork(String network) {
    this.network = network;
  }

  @Override
  public Provider cloneMe() {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Provider applyParentScope(Provider parentScopeProvider) {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Provider applyDefaults() {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void validate() throws ValidationException {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public static URI buildMachineTypeUri(String projectName, String zone, MachineType machineType)
      throws URISyntaxException {
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/zones/%s/machineTypes/%s",
        projectName, zone, machineType.toString()));
  }

  public static URI buildDefaultNetworkUri(String projectName) throws URISyntaxException {
    return buildNetworkUri(projectName, DEFAULT_NETWORK_NAME);
  }

  public static URI buildNetworkUri(String projectName, String networkName) throws URISyntaxException {
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/global/networks/%s",
        projectName, networkName));
  }

  public static URI buildImageUri(ImageType imageType, String imageName) throws URISyntaxException {
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/global/images/%s",
        imageType.toString(), imageName));
  }
}
