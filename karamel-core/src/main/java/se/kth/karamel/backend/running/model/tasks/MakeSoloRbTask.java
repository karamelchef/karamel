/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class MakeSoloRbTask extends Task {

  private final String vendorPath;
  private final String gemsServerUrl;

  public MakeSoloRbTask(MachineRuntime machine, String vendorPath, ClusterStats clusterStats, TaskSubmitter submitter,
                        String gemsServerUrl) {
    super("make solo.rb", "make solo.rb", false, machine, clusterStats, submitter);
    this.vendorPath = vendorPath;
    this.gemsServerUrl = gemsServerUrl;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      String httpProxy = System.getProperty("http.proxy");
      if (httpProxy == null || httpProxy.isEmpty()) {
        httpProxy = "";
      } else {
        httpProxy = "http_proxy \"" + httpProxy + "\"";
      }
      String httpsProxy = System.getProperty("https.proxy");      
      if (httpsProxy == null || httpsProxy.isEmpty()) {
        httpsProxy = "";
      } else {
        httpsProxy = "https_proxy \"" + httpsProxy + "\"";	  
      }
      String gemsUrl = gemsServerUrl;
      String startGemsServer = "";
      if (!gemsUrl.isEmpty()) {
        gemsUrl = "rubygems_url \"" + gemsServerUrl + "/\"";
        URL url = new URL(gemsServerUrl);
        startGemsServer = "setsid /opt/chefdk/embedded/bin/gem server --port " + url.getPort() +
                " --dir /opt/chefdk/embedded/lib/ruby/gems/" + Settings.GEM_SERVER_VERSION + "/gems &";
      }

      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_MAKE_SOLO_RB,
          "install_dir_path", Settings.REMOTE_INSTALL_DIR_PATH(getSshUser()),
          "cookbooks_path", vendorPath,
          "http_proxy", httpProxy,
          "https_proxy", httpsProxy,
          "sudo_command", getSudoCommand(),
          "gems_server_url", gemsUrl,
          "start_gems_server", startGemsServer,
          "pid_file", Settings.PID_FILE_NAME);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId) {
    return "make solo.rb on " + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String id = InstallChefdkTask.makeUniqueId(getMachineId());
    deps.add(id);
    return deps;
  }
}
