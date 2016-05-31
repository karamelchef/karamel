/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.launcher.aws;

/**
 *
 * @author kamal
 */
public enum Region {

  TOKYO("ap-northeast-1", "Asia Pacific", "Tokyo", "ami-6ab58a04", "ami-075f6b69"),
  SEOUL("ap-northeast-2", "Asia Pacific", "Seoul", "ami-11ed237f", "ami-54d11f3a"),
  SINGAPORE("ap-southeast-1", "Asia Pacific", "Singapore", "ami-3126ea52", "ami-3895575b"),
  SYDNEY("ap-southeast-2", "Asia Pacific", "Sydney", "ami-62defb01", "ami-72acf711"),
  FRANKFURT("eu-central-1", "EU", "Frankfurt", "ami-c52a33a9", "ami-de5d42b2"),
  IRELAND("eu-west-1", "EU", "Ireland", "ami-8003a9f3", "ami-94a10ce7"),
  SAO_PAULO("sa-east-1", "South America", "Sao Paulo", "ami-0d119061", "ami-8a4dcae6"),
  N_VIRGINIA("us-east-1", "US East", "N. Virginia", "ami-1a7c5a70", "ami-46e3b42c"),
  N_CALIFORNIA("us-west-1", "US West", "N. California", "ami-c4b8cca4", "ami-3fdcb75f"),
  OREGON("us-west-2", "US West", "Oregon", "ami-aa1efbca", "ami-7a19071b");
  public String code;
  public String continent;
  public String location;
  public String ubuntu_12_04_default_ami;
  public String ubuntu_14_04_default_ami;

  private Region(String code, String continent, String location, String ubuntu_12_04_default_ami, 
      String ubuntu_14_04_default_ami) {
    this.code = code;
    this.continent = continent;
    this.location = location;
    this.ubuntu_12_04_default_ami = ubuntu_12_04_default_ami;
    this.ubuntu_14_04_default_ami = ubuntu_14_04_default_ami;
  }

  public static Region valueByCode(String code) {
    for (Region reg : values()) {
      if (reg.code.equalsIgnoreCase(code)) {
        return reg;
      }
    }
    return null;
  }

}
