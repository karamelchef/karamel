/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class VendorCookbookTask extends Task {

  private final String cookbookName;
  private final String cookbooksHome;
  private final String githubRepoUrl;
  private final String branch;

  public VendorCookbookTask(MachineRuntime machine, ClusterStats clusterStats, TaskSubmitter submitter,
                            String cookbooksHome, KaramelizedCookbook kcb) throws KaramelException {
    super("clone and vendor " + kcb.getCookbookName(),
        "clone and vendor " + kcb.getCookbookName(), true, machine,
        clusterStats, submitter);
    this.cookbookName = kcb.getCookbookName();
    this.cookbooksHome = cookbooksHome;
    this.githubRepoUrl = kcb.getCookbook().getUrls().repoUrl;
    this.branch = kcb.getCookbook().getBranch();
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
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
          "pid_file", Settings.PID_FILE_NAME);
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
