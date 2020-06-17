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
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class VendorCookbookTask extends Task {

  private final String cookbookId;
  private final String cookbooksHome;
  private final String githubRepoName;
  private final String githubRepoUrl;
  private final String subCookbookName;
  private final String branch;

  public VendorCookbookTask(MachineRuntime machine, ClusterStats clusterStats, TaskSubmitter submitter, 
      String cookbookId, String cookbooksHome, String githubRepoUrl, String githubRepoName, String subCookbookName, 
      String branch) {
    super("clone and vendor " + ((subCookbookName == null) ? githubRepoName : subCookbookName), 
        "clone and vendor " + cookbookId, true, machine, 
        clusterStats, submitter);
    this.cookbookId = cookbookId;
    this.cookbooksHome = cookbooksHome;
    this.githubRepoName = githubRepoName;
    this.githubRepoUrl = githubRepoUrl;
    this.subCookbookName = subCookbookName;
    this.branch = branch;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    String cookbookPath = githubRepoName;
    String githubuser = ClusterService.getInstance().getCommonContext().getGithubUsername();    
    if (subCookbookName != null && !subCookbookName.isEmpty()) {
      cookbookPath += Settings.SLASH + subCookbookName;
    }
    if (commands == null) {
      String httpProxy = System.getProperty("http.proxy");
      if (httpProxy == null) {
        httpProxy = "";
      }
      String httpsProxy = System.getProperty("https.proxy");      
      if (httpsProxy == null) {
        httpsProxy = "";
      }
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_CLONE_VENDOR_COOKBOOK,
          "cookbooks_home", cookbooksHome,
          "github_repo_name", githubRepoName,
          "github_username", githubuser,							   
          "cookbook_path", cookbookPath,
          "github_repo_url", githubRepoUrl,
          "branch_name", branch,
          "http_proxy", httpProxy,
          "https_proxy", httpsProxy,
          "vendor_path", Settings.REMOTE_COOKBOOK_VENDOR_PATH(getSshUser(), githubRepoName),
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
    return makeUniqueId(super.getMachineId(), cookbookId);
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String id = MakeSoloRbTask.makeUniqueId(getMachineId());
    deps.add(id);
    return deps;
  }
}
