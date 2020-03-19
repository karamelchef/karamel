/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.launcher.amazon;

import java.util.ArrayList;
import java.util.List;
import org.jclouds.ec2.domain.BlockDeviceMapping;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public enum InstanceType {

  m5a_2xlarge   ("m5a.2xlarge"   , 8   , 1  , DiskType.EBS),
  r5ad_xlarge   ("r5ad.xlarge"   , 4   , 1  , DiskType.NVMe_SSD),
  i3en_12xlarge ("i3en.12xlarge" , 48  , 4  , DiskType.NVMe_SSD),
  i2_xlarge     ("i2.xlarge"     , 4   , 1  , DiskType.SSD),
  t2_micro      ("t2.micro"      , 1   , 1  , DiskType.EBS),
  d2_8xlarge    ("d2.8xlarge"    , 36  , 24 , DiskType.HDD),
  i3en_3xlarge  ("i3en.3xlarge"  , 12  , 1  , DiskType.NVMe_SSD),
  z1d_3xlarge   ("z1d.3xlarge"   , 12  , 1  , DiskType.NVMe_SSD),
  x1e_16xlarge  ("x1e.16xlarge"  , 64  , 1  , DiskType.SSD),
  i2_8xlarge    ("i2.8xlarge"    , 32  , 8  , DiskType.SSD),
  r5a_8xlarge   ("r5a.8xlarge"   , 32  , 1  , DiskType.EBS),
  i2_2xlarge    ("i2.2xlarge"    , 8   , 2  , DiskType.SSD),
  i3en_2xlarge  ("i3en.2xlarge"  , 8   , 2  , DiskType.NVMe_SSD),
  m5a_xlarge    ("m5a.xlarge"    , 4   , 1  , DiskType.EBS),
  p3_2xlarge    ("p3.2xlarge"    , 8   , 1  , DiskType.EBS),
  t2_2xlarge    ("t2.2xlarge"    , 8   , 1  , DiskType.EBS),
  h1_8xlarge    ("h1.8xlarge"    , 32  , 4  , DiskType.HDD),
  r5d_24xlarge  ("r5d.24xlarge"  , 96  , 4  , DiskType.NVMe_SSD),
  i3en_6xlarge  ("i3en.6xlarge"  , 24  , 2  , DiskType.NVMe_SSD),
  r4_8xlarge    ("r4.8xlarge"    , 32  , 1  , DiskType.EBS),
  t2_large      ("t2.large"      , 2   , 1  , DiskType.EBS),
  x1_16xlarge   ("x1.16xlarge"   , 64  , 1  , DiskType.SSD),
  m5a_16xlarge  ("m5a.16xlarge"  , 64  , 1  , DiskType.EBS),
  r5_metal      ("r5.metal"      , 96  , 1  , DiskType.EBS),
  r5a_large     ("r5a.large"     , 2   , 1  , DiskType.EBS),
  c3_large      ("c3.large"      , 2   , 2  , DiskType.SSD),
  r5a_24xlarge  ("r5a.24xlarge"  , 96  , 1  , DiskType.EBS),
  g3_16xlarge   ("g3.16xlarge"   , 64  , 1  , DiskType.EBS),
  a1_2xlarge    ("a1.2xlarge"    , 8   , 1  , DiskType.EBS),
  c4_xlarge     ("c4.xlarge"     , 4   , 1  , DiskType.EBS),
  x1e_4xlarge   ("x1e.4xlarge"   , 16  , 1  , DiskType.SSD),
  m5ad_xlarge   ("m5ad.xlarge"   , 4   , 1  , DiskType.NVMe_SSD),
  i3en_24xlarge ("i3en.24xlarge" , 96  , 8  , DiskType.NVMe_SSD),
  m4_large      ("m4.large"      , 2   , 1  , DiskType.EBS),
  h1_4xlarge    ("h1.4xlarge"    , 16  , 2  , DiskType.HDD),
  x1e_xlarge    ("x1e.xlarge"    , 4   , 1  , DiskType.SSD),
  r3_large      ("r3.large"      , 2   , 1  , DiskType.SSD),
  c4_large      ("c4.large"      , 2   , 1  , DiskType.EBS),
  r5d_xlarge    ("r5d.xlarge"    , 4   , 1  , DiskType.NVMe_SSD),
  r5_2xlarge    ("r5.2xlarge"    , 8   , 1  , DiskType.EBS),
  m3_2xlarge    ("m3.2xlarge"    , 8   , 2  , DiskType.SSD),
  m4_10xlarge   ("m4.10xlarge"   , 40  , 1  , DiskType.EBS),
  m5ad_large    ("m5ad.large"    , 2   , 1  , DiskType.NVMe_SSD),
  r5d_metal     ("r5d.metal"     , 96  , 4  , DiskType.NVMe_SSD),
  x1_32xlarge   ("x1.32xlarge"   , 128 , 2  , DiskType.SSD),
  p3_16xlarge   ("p3.16xlarge"   , 64  , 1  , DiskType.EBS),
  m3_large      ("m3.large"      , 2   , 1  , DiskType.SSD),
  r5_24xlarge   ("r5.24xlarge"   , 96  , 1  , DiskType.EBS),
  r4_large      ("r4.large"      , 2   , 1  , DiskType.EBS),
  r3_xlarge     ("r3.xlarge"     , 4   , 1  , DiskType.SSD),
  x1e_32xlarge  ("x1e.32xlarge"  , 128 , 2  , DiskType.SSD),
  m5a_4xlarge   ("m5a.4xlarge"   , 16  , 1  , DiskType.EBS),
  r5ad_2xlarge  ("r5ad.2xlarge"  , 8   , 1  , DiskType.NVMe_SSD),
  t2_xlarge     ("t2.xlarge"     , 4   , 1  , DiskType.EBS),
  p2_16xlarge   ("p2.16xlarge"   , 64  , 1  , DiskType.EBS),
  c3_8xlarge    ("c3.8xlarge"    , 32  , 2  , DiskType.SSD),
  m3_medium     ("m3.medium"     , 1   , 1  , DiskType.SSD),
  x1e_2xlarge   ("x1e.2xlarge"   , 8   , 1  , DiskType.SSD),
  m5a_8xlarge   ("m5a.8xlarge"   , 32  , 1  , DiskType.EBS),
  m5ad_2xlarge  ("m5ad.2xlarge"  , 8   , 1  , DiskType.NVMe_SSD),
  r5a_2xlarge   ("r5a.2xlarge"   , 8   , 1  , DiskType.EBS),
  m5a_12xlarge  ("m5a.12xlarge"  , 48  , 1  , DiskType.EBS),
  t2_small      ("t2.small"      , 1   , 1  , DiskType.EBS),
  d2_xlarge     ("d2.xlarge"     , 4   , 3  , DiskType.HDD),
  t3_medium     ("t3.medium"     , 2   , 1  , DiskType.EBS),
  m1_xlarge     ("m1.xlarge"     , 4   , 4  , DiskType.HDD),
  m3_xlarge     ("m3.xlarge"     , 4   , 2  , DiskType.SSD),
  m1_medium     ("m1.medium"     , 1   , 1  , DiskType.HDD),
  t3a_medium    ("t3a.medium"    , 2   , 1  , DiskType.EBS),
  r4_16xlarge   ("r4.16xlarge"   , 64  , 1  , DiskType.EBS),
  i3_xlarge     ("i3.xlarge"     , 4   , 1  , DiskType.NVMe_SSD),
  z1d_2xlarge   ("z1d.2xlarge"   , 8   , 1  , DiskType.NVMe_SSD),
  c3_xlarge     ("c3.xlarge"     , 4   , 2  , DiskType.SSD),
  r5_8xlarge    ("r5.8xlarge"    , 32  , 1  , DiskType.EBS),
  c3_2xlarge    ("c3.2xlarge"    , 8   , 2  , DiskType.SSD),
  r3_2xlarge    ("r3.2xlarge"    , 8   , 1  , DiskType.SSD),
  r4_2xlarge    ("r4.2xlarge"    , 8   , 1  , DiskType.EBS),
  p2_8xlarge    ("p2.8xlarge"    , 32  , 1  , DiskType.EBS),
  c4_8xlarge    ("c4.8xlarge"    , 36  , 1  , DiskType.EBS),
  d2_2xlarge    ("d2.2xlarge"    , 8   , 6  , DiskType.HDD),
  d2_4xlarge    ("d2.4xlarge"    , 16  , 12 , DiskType.HDD),
  m5ad_4xlarge  ("m5ad.4xlarge"  , 16  , 2  , DiskType.NVMe_SSD),
  r5_16xlarge   ("r5.16xlarge"   , 64  , 1  , DiskType.EBS),
  m4_xlarge     ("m4.xlarge"     , 4   , 1  , DiskType.EBS),
  r5a_4xlarge   ("r5a.4xlarge"   , 16  , 1  , DiskType.EBS),
  t3_xlarge     ("t3.xlarge"     , 4   , 1  , DiskType.EBS),
  z1d_12xlarge  ("z1d.12xlarge"  , 48  , 2  , DiskType.NVMe_SSD),
  m4_16xlarge   ("m4.16xlarge"   , 64  , 1  , DiskType.EBS),
  r5_12xlarge   ("r5.12xlarge"   , 48  , 1  , DiskType.EBS),
  m4_4xlarge    ("m4.4xlarge"    , 16  , 1  , DiskType.EBS),
  r4_xlarge     ("r4.xlarge"     , 4   , 1  , DiskType.EBS),
  m1_small      ("m1.small"      , 1   , 1  , DiskType.HDD),
  a1_xlarge     ("a1.xlarge"     , 4   , 1  , DiskType.EBS),
  z1d_6xlarge   ("z1d.6xlarge"   , 24  , 1  , DiskType.NVMe_SSD),
  r5d_4xlarge   ("r5d.4xlarge"   , 16  , 2  , DiskType.NVMe_SSD),
  r5a_16xlarge  ("r5a.16xlarge"  , 64  , 1  , DiskType.EBS),
  p2_xlarge     ("p2.xlarge"     , 4   , 1  , DiskType.EBS),
  c3_4xlarge    ("c3.4xlarge"    , 16  , 2  , DiskType.SSD),
  r4_4xlarge    ("r4.4xlarge"    , 16  , 1  , DiskType.EBS),
  r5a_xlarge    ("r5a.xlarge"    , 4   , 1  , DiskType.EBS),
  h1_2xlarge    ("h1.2xlarge"    , 8   , 1  , DiskType.HDD),
  m5ad_24xlarge ("m5ad.24xlarge" , 96  , 4  , DiskType.NVMe_SSD),
  f1_4xlarge    ("f1.4xlarge"    , 16  , 1  , DiskType.NVMe_SSD),
  i3en_xlarge   ("i3en.xlarge"   , 4   , 1  , DiskType.NVMe_SSD),
  r5a_12xlarge  ("r5a.12xlarge"  , 48  , 1  , DiskType.EBS),
  r5ad_24xlarge ("r5ad.24xlarge" , 96  , 4  , DiskType.NVMe_SSD),
  g2_2xlarge    ("g2.2xlarge"    , 8   , 1  , DiskType.SSD),
  c4_2xlarge    ("c4.2xlarge"    , 8   , 1  , DiskType.EBS),
  x1e_8xlarge   ("x1e.8xlarge"   , 32  , 1  , DiskType.SSD),
  h1_16xlarge   ("h1.16xlarge"   , 64  , 8  , DiskType.HDD),
  z1d_xlarge    ("z1d.xlarge"    , 4   , 1  , DiskType.NVMe_SSD),
  r5d_8xlarge   ("r5d.8xlarge"   , 32  , 2  , DiskType.NVMe_SSD),
  t3_2xlarge    ("t3.2xlarge"    , 8   , 1  , DiskType.EBS),
  g3_8xlarge    ("g3.8xlarge"    , 32  , 1  , DiskType.EBS),
  i3_metal      ("i3.metal"      , 72  , 8  , DiskType.NVMe_SSD),
  m4_2xlarge    ("m4.2xlarge"    , 8   , 1  , DiskType.EBS),
  z1d_metal     ("z1d.metal"     , 48  , 2  , DiskType.NVMe_SSD),
  r5ad_large    ("r5ad.large"    , 2   , 1  , DiskType.NVMe_SSD),
  r5_4xlarge    ("r5.4xlarge"    , 16  , 1  , DiskType.EBS),
  r5d_2xlarge   ("r5d.2xlarge"   , 8   , 1  , DiskType.NVMe_SSD),
  r5_large      ("r5.large"      , 2   , 1  , DiskType.EBS),
  i3_large      ("i3.large"      , 2   , 1  , DiskType.NVMe_SSD),
  z1d_large     ("z1d.large"     , 2   , 1  , DiskType.NVMe_SSD),
  i3_16xlarge   ("i3.16xlarge"   , 64  , 8  , DiskType.NVMe_SSD),
  m5ad_12xlarge ("m5ad.12xlarge" , 48  , 2  , DiskType.NVMe_SSD),
  i3en_large    ("i3en.large"    , 2   , 1  , DiskType.NVMe_SSD),
  i2_4xlarge    ("i2.4xlarge"    , 16  , 4  , DiskType.SSD),
  r5_xlarge     ("r5.xlarge"     , 4   , 1  , DiskType.EBS),
  i3_2xlarge    ("i3.2xlarge"    , 8   , 1  , DiskType.NVMe_SSD),
  r5d_large     ("r5d.large"     , 2   , 1  , DiskType.NVMe_SSD),
  g3_4xlarge    ("g3.4xlarge"    , 16  , 1  , DiskType.EBS),
  r5d_12xlarge  ("r5d.12xlarge"  , 48  , 2  , DiskType.NVMe_SSD),
  g2_8xlarge    ("g2.8xlarge"    , 32  , 2  , DiskType.SSD),
  i3_4xlarge    ("i3.4xlarge"    , 16  , 2  , DiskType.NVMe_SSD),
  t3a_small     ("t3a.small"     , 2   , 1  , DiskType.EBS),
  r3_4xlarge    ("r3.4xlarge"    , 16  , 1  , DiskType.SSD),
  m1_large      ("m1.large"      , 2   , 2  , DiskType.HDD),
  m2_4xlarge    ("m2.4xlarge"    , 8   , 2  , DiskType.HDD),
  r3_8xlarge    ("r3.8xlarge"    , 32  , 2  , DiskType.SSD),
  p3_8xlarge    ("p3.8xlarge"    , 32  , 1  , DiskType.EBS),
  m2_xlarge     ("m2.xlarge"     , 2   , 1  , DiskType.HDD),
  c1_medium     ("c1.medium"     , 2   , 1  , DiskType.HDD),
  cc2_8xlarge   ("cc2.8xlarge"   , 32  , 4  , DiskType.HDD),
  t3a_2xlarge   ("t3a.2xlarge"   , 8   , 1  , DiskType.EBS),
  c1_xlarge     ("c1.xlarge"     , 8   , 4  , DiskType.HDD),
  r5d_16xlarge  ("r5d.16xlarge"  , 64  , 4  , DiskType.NVMe_SSD),
  m2_2xlarge    ("m2.2xlarge"    , 4   , 1  , DiskType.HDD),
  r5ad_4xlarge  ("r5ad.4xlarge"  , 16  , 2  , DiskType.NVMe_SSD),
  t3a_nano      ("t3a.nano"      , 2   , 1  , DiskType.EBS),
  r5ad_12xlarge ("r5ad.12xlarge" , 48  , 2  , DiskType.NVMe_SSD),
  m5a_large     ("m5a.large"     , 2   , 1  , DiskType.EBS),
  i3_8xlarge    ("i3.8xlarge"    , 32  , 4  , DiskType.NVMe_SSD),
  cr1_8xlarge   ("cr1.8xlarge"   , 32  , 2  , DiskType.SSD),
  m5a_24xlarge  ("m5a.24xlarge"  , 96  , 1  , DiskType.EBS),
  c4_4xlarge    ("c4.4xlarge"    , 16  , 1  , DiskType.EBS),
  t3_small      ("t3.small"      , 2   , 1  , DiskType.EBS),
  f1_16xlarge   ("f1.16xlarge"   , 64  , 4  , DiskType.NVMe_SSD),
  t3_micro      ("t3.micro"      , 2   , 1  , DiskType.EBS),
  t3_large      ("t3.large"      , 2   , 1  , DiskType.EBS),
  t3a_micro     ("t3a.micro"     , 2   , 1  , DiskType.EBS),
  g3s_xlarge    ("g3s.xlarge"    , 4   , 1  , DiskType.EBS),
  a1_4xlarge    ("a1.4xlarge"    , 16  , 1  , DiskType.EBS),
  f1_2xlarge    ("f1.2xlarge"    , 8   , 1  , DiskType.NVMe_SSD),
  a1_medium     ("a1.medium"     , 1   , 1  , DiskType.EBS),
  t2_medium     ("t2.medium"     , 2   , 1  , DiskType.EBS),
  a1_large      ("a1.large"      , 2   , 1  , DiskType.EBS),
  hs1_8xlarge   ("hs1.8xlarge"   , 17  , 24 , DiskType.HDD),
  t3a_xlarge    ("t3a.xlarge"    , 4   , 1  , DiskType.EBS),
  t1_micro      ("t1.micro"      , 1   , 1  , DiskType.EBS),
  u_6tb1_metal  ("u-6tb1.metal"  , 448 , 1  , DiskType.EBS),
  t3_nano       ("t3.nano"       , 2   , 1  , DiskType.EBS),
  t2_nano       ("t2.nano"       , 1   , 1  , DiskType.EBS),
  t3a_large     ("t3a.large"     , 2   , 1  , DiskType.EBS),
  u_9tb1_metal  ("u-9tb1.metal"  , 448 , 1  , DiskType.EBS),
  u_12tb1_metal ("u-12tb1.metal" , 448 , 1  , DiskType.EBS),
  c5d_xlarge    ("c5d.xlarge"    , 4   , 1  , DiskType.NVMe_SSD),
  c5_metal      ("c5.metal"      , 96  , 1  , DiskType.EBS),
  c5_9xlarge    ("c5.9xlarge"    , 36  , 1  , DiskType.EBS),
  m5_24xlarge   ("m5.24xlarge"   , 96  , 1  , DiskType.EBS),
  m5d_12xlarge  ("m5d.12xlarge"  , 48  , 2  , DiskType.NVMe_SSD),
  c5_large      ("c5.large"      , 2   , 1  , DiskType.EBS),
  c5n_large     ("c5n.large"     , 2   , 1  , DiskType.EBS),
  c5_24xlarge   ("c5.24xlarge"   , 96  , 1  , DiskType.EBS),
  m5_2xlarge    ("m5.2xlarge"    , 8   , 1  , DiskType.EBS),
  c5_18xlarge   ("c5.18xlarge"   , 72  , 1  , DiskType.EBS),
  c5d_18xlarge  ("c5d.18xlarge"  , 72  , 2  , DiskType.NVMe_SSD),
  c5n_18xlarge  ("c5n.18xlarge"  , 72  , 1  , DiskType.EBS),
  m5_large      ("m5.large"      , 2   , 1  , DiskType.EBS),
  c5_4xlarge    ("c5.4xlarge"    , 16  , 1  , DiskType.EBS),
  m5d_24xlarge  ("m5d.24xlarge"  , 96  , 4  , DiskType.NVMe_SSD),
  m5d_large     ("m5d.large"     , 2   , 1  , DiskType.NVMe_SSD),
  m5d_2xlarge   ("m5d.2xlarge"   , 8   , 1  , DiskType.NVMe_SSD),
  c5d_large     ("c5d.large"     , 2   , 1  , DiskType.NVMe_SSD),
  c5n_2xlarge   ("c5n.2xlarge"   , 8   , 1  , DiskType.EBS),
  m5d_xlarge    ("m5d.xlarge"    , 4   , 1  , DiskType.NVMe_SSD),
  m5_metal      ("m5.metal"      , 96  , 1  , DiskType.EBS),
  m5_12xlarge   ("m5.12xlarge"   , 48  , 1  , DiskType.EBS),
  c5d_4xlarge   ("c5d.4xlarge"   , 16  , 1  , DiskType.NVMe_SSD),
  c5n_xlarge    ("c5n.xlarge"    , 4   , 1  , DiskType.EBS),
  p3dn_24xlarge ("p3dn.24xlarge" , 96  , 2  , DiskType.NVMe_SSD),
  m5d_16xlarge  ("m5d.16xlarge"  , 64  , 4  , DiskType.NVMe_SSD),
  c5_2xlarge    ("c5.2xlarge"    , 8   , 1  , DiskType.EBS),
  c5d_2xlarge   ("c5d.2xlarge"   , 8   , 1  , DiskType.NVMe_SSD),
  m5_xlarge     ("m5.xlarge"     , 4   , 1  , DiskType.EBS),
  c5n_9xlarge   ("c5n.9xlarge"   , 36  , 1  , DiskType.EBS),
  c5_xlarge     ("c5.xlarge"     , 4   , 1  , DiskType.EBS),
  m5d_4xlarge   ("m5d.4xlarge"   , 16  , 2  , DiskType.NVMe_SSD),
  m5d_metal     ("m5d.metal"     , 96  , 4  , DiskType.NVMe_SSD),
  m5_4xlarge    ("m5.4xlarge"    , 16  , 1  , DiskType.EBS),
  m5d_8xlarge   ("m5d.8xlarge"   , 32  , 2  , DiskType.NVMe_SSD),
  m5_16xlarge   ("m5.16xlarge"   , 64  , 1  , DiskType.EBS),
  c5d_9xlarge   ("c5d.9xlarge"   , 36  , 1  , DiskType.NVMe_SSD),
  c5d_12xlarge  ("c5d.12xlarge"  , 48  , 2  , DiskType.NVMe_SSD),
  c5_12xlarge   ("c5.12xlarge"   , 48  , 1  , DiskType.EBS),
  c5n_4xlarge   ("c5n.4xlarge"   , 16  , 1  , DiskType.EBS),
  m5_8xlarge    ("m5.8xlarge"    , 32  , 1  , DiskType.EBS);

  public final String model;
  public final Integer cores;
  public final Integer numDisks;
  public final DiskType diskType;

  private InstanceType(String model, Integer cores, Integer numDisks,
                       DiskType diskType) {
    this.model = model;
    this.numDisks = numDisks;
    this.cores = cores;
    this.diskType = diskType;
  }

  public static InstanceType valueByModel(String model) {
    String m = model.replaceAll("\\.", "_");
    m = m.replaceAll("-", "_");
    return valueOf(m);
  }

  public StorageDevice[] getStorageDevices() {
//    if (diskType == DiskType.EBS) {
//      return new StorageDevice[0];
//    }

    StorageDevice[] devices = new StorageDevice[numDisks];
    for (int i = 0; i < numDisks; i++) {
      if ( diskType == DiskType.HDD ){
        devices[i] = new StorageDevice(Settings.AWS_STORAGE_MAPPINGNAME_PREFIX+ (char)('b'+i),
                "/mnt" +
                "/disk"+i, Settings.AWS_STORAGE_VIRTUALNAME_PREFIX +i);
      } else if ( diskType == DiskType.SSD ){
        devices[i] = new StorageDevice(Settings.AWS_SSD_MAPPINGNAME_PREFIX+ (char)('b'+i),
                "/mnt" +
                "/ssd"+i, Settings.AWS_STORAGE_VIRTUALNAME_PREFIX +i);
      } else if (diskType == DiskType.NVMe_SSD || diskType == DiskType.EBS){
        devices[i] = new StorageDevice(Settings.AWS_NVME_MAPPINGNAME_PREFIX+ i+"n1", "/mnt" +
                "/nvme_ssd"+i, Settings.AWS_STORAGE_VIRTUALNAME_PREFIX +i);
      } else {
        throw new IllegalArgumentException("Disk type not supported");
      }
    }
    return devices;
  }

  public List<BlockDeviceMapping> getEphemeralDeviceMappings() {
    ArrayList<BlockDeviceMapping> maps = new ArrayList<>();
    if (numDisks != null) {
      for (StorageDevice device : getStorageDevices()) {
        BlockDeviceMapping map
            = new BlockDeviceMapping.MapEphemeralDeviceToDevice(device.mappingName(), device.virtualName());
        maps.add(map);
      }
    }
    return maps;
  }
}
