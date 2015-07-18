/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.api;

import java.util.List;
import se.kth.karamel.backend.Experiment;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.backend.github.GithubUser;
import se.kth.karamel.backend.github.OrgItem;
import se.kth.karamel.backend.github.RepoItem;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 * The main API of Karamel-Core for Karamel clients
 *
 * @author kamal
 */
public interface KaramelApi {

  /**
   * Demonstrates available commands and their usage
   *
   * @return
   * @throws KaramelException
   */
  public String commandCheatSheet() throws KaramelException;

  /**
   * Parses the command, if valid fetches the result in string, result could have different formatting depends on the
   * command.
   *
   * @param command
   * @param args
   * @return
   * @throws KaramelException
   */
  public CommandResponse processCommand(String command, String... args) throws KaramelException;

  /**
   * Returns visible recipes and attributes of the cookbook with their detail as a json file
   *
   * @param cookbookUrl
   * @param refresh
   * @return
   * @throws KaramelException
   */
  public String getCookbookDetails(String cookbookUrl, boolean refresh) throws KaramelException;

  /**
   * Converts json definition of the cluster into a yaml object
   *
   * @param json
   * @return
   * @throws KaramelException
   */
  public String jsonToYaml(String json) throws KaramelException;

  /**
   * Converts yaml definition of the cluster into the json
   *
   * @param yaml
   * @return
   * @throws KaramelException
   */
  public String yamlToJson(String yaml) throws KaramelException;

  /**
   * Loads Karamel common keys
   *
   * @param passphrase user-supplied password for ssh private key
   * @return
   * @throws KaramelException
   */
  public SshKeyPair loadSshKeysIfExist() throws KaramelException;

  /**
   * Loads cluster specific keys
   *
   * @param clusterName
   * @param passphrase user-supplied password for ssh private key
   * @return
   * @throws KaramelException
   */
  public SshKeyPair loadSshKeysIfExist(String clusterName) throws KaramelException;

  /**
   * Generates a common ssh keys in the karamel folder
   *
   * @return
   * @throws KaramelException
   */
  public SshKeyPair generateSshKeysAndUpdateConf() throws KaramelException;

  /**
   * Generates cluster specific ssh keys
   *
   * @param clusterName
   * @return
   * @throws KaramelException
   */
  public SshKeyPair generateSshKeysAndUpdateConf(String clusterName) throws KaramelException;

  /**
   * Register ssh keys for the current runtime of karamel
   *
   * @param keypair
   * @return
   * @throws KaramelException
   */
  public SshKeyPair registerSshKeys(SshKeyPair keypair) throws KaramelException;

  /**
   * Register ssh keys for the specified cluster
   *
   * @param clusterName
   * @param keypair
   * @return
   * @throws KaramelException
   */
  public SshKeyPair registerSshKeys(String clusterName, SshKeyPair keypair) throws KaramelException;

  /**
   * Reads it from default karamel conf file
   *
   * @return
   * @throws KaramelException
   */
  public Ec2Credentials loadEc2CredentialsIfExist() throws KaramelException;

  /**
   * Validates user's credentials before starting the cluster
   *
   * @param credentials
   * @return
   * @throws KaramelException
   */
  public boolean updateEc2CredentialsIfValid(Ec2Credentials credentials) throws KaramelException;

  /**
   * Starts running the cluster by launching machines and installing softwares It expect to receive a complete
   * cluster-json for the cluster.
   *
   * @param json
   * @throws KaramelException
   */
  public void startCluster(String json) throws KaramelException;

  /**
   * In case user wants to pause the running cluster for inspection reasons. It implies that machines won't receive any
   * new ssh command form the karamel-core. User can either purge or resume a paused cluster.
   *
   * @param clusterName
   * @throws KaramelException
   */
  public void pauseCluster(String clusterName) throws KaramelException;

  /**
   * It resumes an already paused cluster, machines will go on and run ssh commands.
   *
   * @param clusterName
   * @throws KaramelException
   */
  public void resumeCluster(String clusterName) throws KaramelException;

  /**
   * It stops sending new ssh command to machines, destroys the automatic allocated machines and disconnects ssh clients
   * from machines. User, however, shouldn't expect that bare-metal machines be destroyed as well.
   *
   * @param clusterName
   * @throws KaramelException
   */
  public void purgeCluster(String clusterName) throws KaramelException;

  /**
   * Returns a json containing all groups, machines, their status, tasks/commands and their status.
   *
   * @param clusterName
   * @return
   * @throws KaramelException
   */
  public String getClusterStatus(String clusterName) throws KaramelException;

  /**
   * Returns installation flow DAG that each node is a task assigned to a certain machine with the current status of the
   * task.
   *
   * @param clusterName
   * @return
   * @throws KaramelException
   */
  public String getInstallationDag(String clusterName) throws KaramelException;

  /**
   * Register password for Baremetal sudo account
   *
   * @param password
   * @throws KaramelException
   */
  public void registerSudoPassword(String password) throws KaramelException;

  /**
   * Register username/password for github account
   *
   * @param user github account name 
   * @param password github password
   * @return GithubUser Json object also containing primary github email address
   * @throws KaramelException
   */
  public GithubUser registerGithubAccount(String user, String password) throws KaramelException;

  /**
   * Load any existing credentials stored locally
   *
   * @return GithubUser object
   * @throws KaramelException
   */
  public GithubUser loadGithubCredentials() throws KaramelException;

  /**
   * Lists the available repos in a github organization.
   *
   * @param organization
   * @return List of available repos
   * @throws KaramelException
   */
  public List<RepoItem> listGithubRepos(String organization) throws KaramelException;

  /**
   * Lists the available organizations for a user in github. Must call 'registerGithubAccount' first.
   *
   * @return List of available orgs
   * @throws KaramelException
   */
  public List<OrgItem> listGithubOrganizations() throws KaramelException;

//  /**
//   * Create a new github repo in an organization
//   *
//   * @param organization if organization is empty or null, create the repo for the authenticated user
//   * @param repo the name of the repo to create
//   * @param description of what's in the repository
//   * @throws KaramelException
//   */
//  public void createGithubRepo(String organization, String repo, String description) throws KaramelException;
  /**
   * Add a file to an existing repo, commit it, and push it to github.
   *
   * @param owner organization or user
   * @param repoName name of repo
   * @param experiment bash scripts and config files to add, commit, and push.
   * @throws KaramelException
   */
  public void commitAndPushExperiment(Experiment experiment)
      throws KaramelException;

  /**
   * Loads an experiment into the Designer, given its clone URL
   * @param githubRepoUrl url for github repo
   * @return Json object for the ExperimentContext
   * @throws se.kth.karamel.common.exception.KaramelException 
   */
  public Experiment loadExperiment(String githubRepoUrl) throws KaramelException;

  /**
   *
   * @param org github org name
   * @param repo github repo name
   * @param description repo description
   * @return RepoItem bean/json containing name, description of repo.
   * @throws KaramelException
   */
  public RepoItem createGithubRepo(String org, String repo, String description) throws KaramelException;

  /**
   * 
   * @param owner
   * @param repo
   * @param experimentName
   */
  public void removeFileFromExperiment(String owner, String repo, String experimentName) ;

  
  /**
   * 
   * @param owner
   * @param repo 
   * @param removeGitHub 
   * @param removeLocal 
   */
  public void removeRepo(String owner, String repo, boolean removeLocal, boolean removeGitHub) throws KaramelException;
  
}
