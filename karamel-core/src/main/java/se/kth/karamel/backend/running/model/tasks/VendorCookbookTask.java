package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

public class VendorCookbookTask extends Task {

  private final String cookbookName;
  private final String cookbooksHome;
  private final String githubRepoUrl;
  private final String branch;

  public VendorCookbookTask(MachineRuntime machine, ClusterStats clusterStats, TaskSubmitter submitter,
                            String cookbooksHome, String cookbookName, Cookbook cookbook)
      throws KaramelException {
    super("clone and vendor " + cookbookName,
        "clone and vendor " + cookbookName, true, machine,
        clusterStats, submitter);
    this.cookbookName = cookbookName;
    this.cookbooksHome = cookbooksHome;
    this.githubRepoUrl = Settings.GITHUB_BASE_URL + "/" + cookbook.getGithub();
    this.branch = cookbook.getBranch();
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    boolean airgap = Boolean.parseBoolean(conf.getProperty(Settings.KARAMEL_AIRGAP));
    if (commands == null) {
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_CLONE_VENDOR_COOKBOOK,
          "cookbooks_home", cookbooksHome,
          "github_repo_name", cookbookName,
          "cookbook_path", cookbookName,
          "github_repo_url", githubRepoUrl,
          "branch_name", branch,
          "vendor_path", Settings.REMOTE_COOKBOOK_VENDOR_PATH(getSshUser(), cookbookName),
          "sudo_command", getSudoCommand(),
          "task_id", getId(),
          "install_dir_path", Settings.REMOTE_INSTALL_DIR_PATH(getSshUser()),          
          "succeedtasks_filepath", Settings.SUCCEED_TASKLIST_FILENAME,
          "pid_file", Settings.PID_FILE_NAME,
          "is_airgap", String.valueOf(airgap));
    }
    return commands;
  }

  public static String makeUniqueId(String machineId, String cookbookId) {
    return "clone and vendor " + cookbookId + " on " + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId(), cookbookName);
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String id = MakeSoloRbTask.makeUniqueId(getMachineId());
    deps.add(id);
    return deps;
  }
}
