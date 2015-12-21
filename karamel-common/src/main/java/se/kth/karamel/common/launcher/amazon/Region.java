/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.launcher.amazon;

/**
 *
 * @author kamal
 */
public enum Region {

  TOKYO("ap-northeast-1", "Asia Pacific", "Tokyo", "ami-075f6b69"),
  SINGAPORE("ap-southeast-1", "Asia Pacific", "Singapore", "ami-3895575b"),
  SYDNEY("ap-southeast-2", "Asia Pacific", "Sydney", "ami-72acf711"),
  FRANKFURT("eu-central-1", "EU", "Frankfurt", "ami-de5d42b2"),
  IRELAND("eu-west-1", "EU", "Ireland", "ami-94a10ce7"),
  SAO_PAULO("sa-east-1", "South America", "Sao Paulo", "ami-8a4dcae6"),
  N_VIRGINIA("us-east-1", "US East", "N. Virginia", "ami-46e3b42c"),
  N_CALIFORNIA("us-west-1", "US West", "N. California", "ami-3fdcb75f"),
  OREGON("us-west-2", "US West", "Oregon", "ami-7a19071b");
  public String code;
  public String continent;
  public String location;
  public String ubuntu_12_04_default_ami;

  private Region(String code, String continent, String location, String ubuntu_12_04_default_ami) {
    this.code = code;
    this.continent = continent;
    this.location = location;
    this.ubuntu_12_04_default_ami = ubuntu_12_04_default_ami;
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
