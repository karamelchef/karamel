/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef;

import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.launcher.amazon.Region;

/**
 *
 * @author kamal
 */
public class Ec2 extends Provider {

  private String type;
  private String region;
  private String ami;
  private Float price;
  private String vpc;
  private String subnet;
  private String iamarn;
  private String zone;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getAmi() {
    return ami;
  }

  public void setAmi(String ami) {
    this.ami = ami;
  }

  public Float getPrice() {
    return price;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

  public String getSubnet() {
    return subnet;
  }

  public void setSubnet(String subnet) {
    this.subnet = subnet;
  }

  public String getVpc() {
    return vpc;
  }

  public void setVpc(String vpc) {
    this.vpc = vpc;
  }

  public String getIamarn() {
    return iamarn;
  }

  public void setIamarn(String iamarn) {
    this.iamarn = iamarn;
  }

  public static Ec2 makeDefault() {
    Ec2 ec2 = new Ec2();
    return ec2.applyDefaults();
  }

  public String getZone() {
    return zone;
  }

  public void setZone(String zone) {
    this.zone = zone;
  }

  @Override
  public Ec2 applyDefaults() {
    Ec2 clone = cloneMe();
    if (clone.getUsername() == null) {
      clone.setUsername(Settings.AWS_VM_USERNAME_DEFAULT);
    }
    if (clone.getRegion() == null) {
      clone.setRegion(Settings.AWS_REGION_CODE_DEFAULT);
    }

    if (clone.getAmi() == null) {
      Region reg = Region.valueByCode(clone.getRegion());
      clone.setAmi(reg.ubuntu_12_04_default_ami);
    }
    if (clone.getType() == null) {
      clone.setType(Settings.AWS_VM_TYPE_DEFAULT);
    }
    return clone;
  }

  @Override
  public Ec2 cloneMe() {
    Ec2 ec2 = new Ec2();
    ec2.setUsername(getUsername());
    ec2.setAmi(ami);
    ec2.setRegion(region);
    ec2.setType(type);
    ec2.setPrice(price);
    ec2.setSubnet(subnet);
    ec2.setVpc(vpc);
    ec2.setIamarn(iamarn);
    ec2.setZone(zone);
    return ec2;
  }

  @Override
  public Ec2 applyParentScope(Provider parentScopeProvider) {
    Ec2 clone = cloneMe();
    if (parentScopeProvider instanceof Ec2) {
      Ec2 parentEc2 = (Ec2) parentScopeProvider;
      if (clone.getUsername() == null) {
        clone.setUsername(parentEc2.getUsername());
      }
      if (clone.getAmi() == null) {
        clone.setAmi(parentEc2.getAmi());
      }
      if (clone.getRegion() == null) {
        clone.setRegion(parentEc2.getRegion());
      }
      if (clone.getType() == null) {
        clone.setType(parentEc2.getType());
      }
      if (clone.getPrice() == null) {
        clone.setPrice(parentEc2.getPrice());
      }
      if (clone.getSubnet() == null) {
        clone.setSubnet(parentEc2.getSubnet());
      }
      if (clone.getVpc() == null) {
        clone.setVpc(parentEc2.getVpc());
      }
      if (clone.getIamarn() == null) {
        clone.setIamarn(parentEc2.getIamarn());
      }
      if (clone.getZone() == null) {
        clone.setZone(parentEc2.getZone());
      }
    }
    return clone;
  }

  @Override
  public void validate() throws ValidationException {
    if ((subnet != null && vpc == null) || (subnet == null && vpc != null)) {
      throw new ValidationException("Both subnet and vpc ids are required for vpc settings on ec2");
    }
  }

}
