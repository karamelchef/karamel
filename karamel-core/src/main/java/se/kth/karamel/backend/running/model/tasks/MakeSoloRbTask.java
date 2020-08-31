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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
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
      String gemsServerPort = "";
      if (!gemsUrl.isEmpty()) {
        logger.info("Starting gems server");
        gemsUrl = "rubygems_url \"" + gemsServerUrl + "/\"";
        URL url = new URL(gemsServerUrl);
        gemsServerPort = Integer.toString(url.getPort());
        startGemsServer(url.getPort());
        // Note: stdout/stdin have to be redirected for this command, otherwise the SSH connection will remain open
        // https://superuser.com/questions/449193/nohup-over-ssh-wont-return
//      startGemsServer = "nohup /opt/chefdk/embedded/bin/gem server --port " + url.getPort() +
//      " --dir /opt/chefdk/embedded/lib/ruby/gems/" + Settings.GEM_SERVER_VERSION + "/gems  >/dev/null 2>&1 &";
      }

      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_MAKE_SOLO_RB,
        "install_dir_path", Settings.REMOTE_INSTALL_DIR_PATH(getSshUser()),
        "cookbooks_path", vendorPath,
        "http_proxy", httpProxy,
        "https_proxy", httpsProxy,
        "sudo_command", getSudoCommand(),
        "gems_server_url", gemsUrl,
        "gem_server_port", gemsServerPort,
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

  private void startGemsServer(Integer port) {
    List<String> subcommands = new ArrayList<>();
    String path = "/opt/chefdk/embedded/lib/ruby/gems/" + Settings.GEM_SERVER_VERSION + "/gems";
    subcommands.add(getSudoCommand());
    subcommands.add("nohup");
    subcommands.add("/opt/chefdk/embedded/bin/gem server");
    subcommands.add("--port");
    subcommands.add(port.toString());
    subcommands.add("--dir");
    subcommands.add(path);
    subcommands.add(">/dev/null");
    subcommands.add("2>&1");
    subcommands.add("&");

    logger.info("Gem server command: ");
    subcommands.forEach(System.out::println);

    File cwd = new File(path);

    ProcessBuilder processBuilder = new ProcessBuilder(subcommands);
    processBuilder.directory(cwd);
    processBuilder.redirectErrorStream(true);

    Process process = null;
    try {
      process = processBuilder.start();
    } catch (IOException e) {
      e.printStackTrace();
      logger.error("Could not start gem server");
    }
//        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
//        boolean ignoreStreams = true;
//
//        StreamGobbler stderrGobbler;
//        StreamGobbler stdoutGobbler;
//
//        stderrGobbler = new StreamGobbler(process.getErrorStream(), errStream, ignoreStreams);
//        stdoutGobbler = new StreamGobbler(process.getInputStream(), outStream, ignoreStreams);
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        executorService.submit(stderrGobbler);
//
//        ExecutorService executorService2 = Executors.newSingleThreadExecutor();
//        executorService2.submit(stdoutGobbler);

  }

  private class StreamGobbler implements Runnable {
    private final static int KB = 1024;

    private final InputStream in;
    private final OutputStream out;
    private final byte[] buffer;
    private final boolean ignoreStream;

    public StreamGobbler(InputStream in, OutputStream out, boolean ignoreStream) {
      this.in = in;
      this.out = out;
      this.ignoreStream = ignoreStream;
      this.buffer = new byte[4 * KB];
    }

    @Override
    public void run() {
      int bytesRead = 0;
      try (BufferedInputStream bis = new BufferedInputStream(in)) {
        while ((bytesRead = bis.read(buffer)) != -1) {
          if (!ignoreStream) {
            out.write(buffer, 0, bytesRead);
          }
        }
      } catch (IOException ex) {
        ex.printStackTrace(new PrintStream(out));
      }
    }
  }

}
