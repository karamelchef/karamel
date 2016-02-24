package se.kth.karamel.common.util;

import se.kth.karamel.common.exception.UnsupportedImageType;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Hooman
 */
public class GceSettings {

  public static final String DEFAULT_NETWORK_NAME = "default";
  public static final String DEFAULT_IMAGE = "ubuntu-1404-trusty-v20150316";
  public static final String DEFAULT_ZONE = "europe-west1-b";
  public static final String DEFAULT_MACHINE_TYPE = MachineType.n1_standard_1.toString();

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

  public static URI buildMachineTypeUri(String projectName, String zone, String machineType)
      throws URISyntaxException {
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/zones/%s/machineTypes/%s",
        projectName, zone, machineType));
  }

  public static URI buildDefaultNetworkUri(String projectName) throws URISyntaxException {
    return buildNetworkUri(projectName, DEFAULT_NETWORK_NAME);
  }

  public static URI buildNetworkUri(String projectName, String networkName) throws URISyntaxException {
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/global/networks/%s",
        projectName, networkName));
  }

  public static URI buildImageUri(String imageName) throws URISyntaxException, UnsupportedImageType {
    ImageType type;
    if (imageName.contains("ubuntu")) {
      type = ImageType.ubuntu;
    } else if (imageName.contains("debian")) {
      type = ImageType.debian;
    } else if (imageName.contains("centos")) {
      type = ImageType.centos;
    } else if (imageName.contains("ceros")) {
      type = ImageType.ceros;
    } else if (imageName.contains("opensuse")) {
      type = ImageType.opensuse;
    } else if (imageName.contains("rhel")) {
      type = ImageType.redhat;
    } else if (imageName.contains("suse")) {
      type = ImageType.suse;
    } else {
      throw new UnsupportedImageType(String.format("No image type is found for image %s", imageName));
    }
    return new URI(String.format("https://www.googleapis.com/compute/v1/projects/%s/global/images/%s",
        type.toString(), imageName));
  }
}
