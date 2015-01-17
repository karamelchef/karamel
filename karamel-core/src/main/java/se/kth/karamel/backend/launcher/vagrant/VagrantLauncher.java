/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.launcher.vagrant;


/**
 *
 * @author kamal
 */
public class VagrantLauncher {
//
//  String ipsText;
//  String privateKey;
//
//  public VagrantProvisioner(ClusterMeta cluster, Properties props) {
//    super(cluster, props);
//  }
//
//  @Override
//  public void configure(Properties confs1) {
//    Properties confs = Confs.loadVagrantConfs();
//    confs.putAll(confs1);
//    super.configure(confs);
//    ipsText = confs.getProperty(Confs.VAGRANT_MACHINES_KEY);
//    privateKey = confs.getProperty(SSH_PRIKEY_KEY);
//  }
//
//  @Override
//  public void cleanup() {
//  }
//
//  @Override
//  public void forkGroups() {
//  }
//
//  @Override
//  public boolean forkMachines() {
//    boolean status = false;
//    List<MachineInfo> infos = new ArrayList<>();
//    String[] ranges = ipsText.replaceAll("\\n", ",").split(",");
//    for (String range : ranges) {
//      String user = "vagrant";
//      if (range.contains("@")) {
//        String[] sp = range.split("@");
//        user = sp[0];
//        range = sp[1];
//      }
//      List<String> ips = null;
//      if (range.contains("-")) {
//        String[] sp = range.split("-");
//        ips = IpConverter.ipRange(sp[0], sp[1]);
//      } else {
//        ips = new ArrayList<>();
//        ips.add(range);
//      }
//      for (String ip : ips) {
//        MachineInfo machine = new MachineInfo();
//        List<String> publicIps = new ArrayList<String>();
//        publicIps.add(ip);
//        machine.setPublicIps(publicIps);
//        List<String> privateIps = new ArrayList<String>();
//        privateIps.add(ip);
//        machine.setPrivateIps(privateIps);
//        machine.setSshPort(22);
//        machine.setSshUser(user);
//        machine.setPrivateKey(privateKey);
//        machine.setPublicKey(publicKey);
//        machine.setLogPrefix(ip + ": ");
//        infos.add(machine);
//      }
//    }
//
//    try {
//      int index = 0;
//      for (GroupMeta group : cluster.getGroups()) {
//        List<MachineInfo> machines = new ArrayList<>();
//        for (int i = 1; i <= group.getSizeInt(); i++) {
//          MachineInfo machine = infos.get(index++);
//          machines.add(machine);
//          ArrayList<String> privateIps = new ArrayList();
//          ArrayList<String> publicIps = new ArrayList();
//          privateIps.addAll(machine.getPrivateIps());
//          publicIps.addAll(machine.getPublicIps());
//        }
//        group.setMachines(machines);
//      }
//      status = true;
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//    return status;
//  }

}
