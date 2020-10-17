/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kamal
 */
public class MakeSoloRbTask extends Task {

  private static final Logger logger = Logger.getLogger(MakeSoloRbTask.class);
  private final String vendorPath;
  private final String gemsServerUrl;

  public MakeSoloRbTask(MachineRuntime machine, String vendorPath, ClusterStats clusterStats, TaskSubmitter submitter,
                        String gemsServerUrl) {
    super("make solo.rb", "make solo.rb", false, machine, clusterStats, submitter);
    this.vendorPath = vendorPath;
    this.gemsServerUrl = gemsServerUrl;
  }

  public static String makeUniqueId(String machineId) {
    return "make solo.rb on " + machineId;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      String httpProxy = System.getProperty("http.proxy", "");
      String httpProxyCopy=httpProxy;
      if (!httpProxy.isEmpty()) {
        httpProxy = "http_proxy \"" + httpProxy + "\"";
      }
      String httpsProxy = System.getProperty("https.proxy", "");
      if (! httpsProxy.isEmpty()) {
        if (httpProxy.isEmpty()) {
          httpProxy = "https_proxy \"" + httpsProxy + "\"";
        }
        httpsProxy = "https_proxy \"" + httpsProxy + "\"";
        // solo.rb wants the http_proxy set as well for download the gems
      }
      String gemsUrl = gemsServerUrl;
      String startGemsServer = "";
      String gemsServerPort = "";
      String gemsDir = "";
      String gemsSrcDir = "";
      if (!gemsUrl.isEmpty()) {
        gemsUrl = "rubygems_url \"" + gemsServerUrl + "/\"";
        URL url = new URL(gemsServerUrl);
        gemsServerPort = Integer.toString(url.getPort());
        // user.dir is the CWD, which should be the karamel-0.6 directory.
        gemsSrcDir = System.getProperty("user.dir") + "/repo/gems";
        String baseDir = "/opt/chefdk/embedded/lib/ruby/gems/" + Settings.GEM_SERVER_VERSION;
        gemsDir = baseDir + "/cache";
        // Note: stdout/stdin have to be redirected for this command, otherwise the SSH connection will remain open
        // https://superuser.com/questions/449193/nohup-over-ssh-wont-return
        startGemsServer = "nohup /opt/chefdk/embedded/bin/gem server --port " + url.getPort() +
          " --dir " + baseDir + ",/root/.chefdk/gem/ruby/2.5.0 >/dev/null 2>&1 &";
      }

      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_MAKE_SOLO_RB,
        "install_dir_path", Settings.REMOTE_INSTALL_DIR_PATH(getSshUser()),
        "cookbooks_path", vendorPath,
        "http_proxy", httpProxy,
        "https_proxy", httpsProxy,
        "sudo_command", getSudoCommand(),
        "gems_dir", gemsDir,
        "gems_src_dir", gemsSrcDir,
        "gems_server_url", gemsUrl,
        "gems_server_port", gemsServerPort,
        "start_gems_server", startGemsServer,
        "pid_file", Settings.PID_FILE_NAME);
    }
    return commands;
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

  @Override
  public boolean isSudoTerminalReqd() {
    return true;
  }
}
