package se.kth.karamel.client.api;

import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.backend.github.GithubUser;
import se.kth.karamel.backend.github.OrgItem;
import se.kth.karamel.backend.github.RepoItem;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.OcciCredentials;
import se.kth.karamel.common.util.NovaCredentials;
import se.kth.karamel.common.util.SshKeyPair;

import java.util.List;

/**
 * The main API of Karamel-Core for Karamel clients
 *
 */
public interface KaramelApi {

  /**
   * Demonstrates available commands and their usage
   *
   * @return
   * @throws KaramelException
   */
  String commandCheatSheet() throws KaramelException;

  /**
   * Parses the command, if valid fetches the result in string, result could have different formatting depends on the
   * command.
   *
   * @param command
   * @param args
   * @return
   * @throws KaramelException
   */
  CommandResponse processCommand(String command, String... args) throws KaramelException;

  /**
   * Returns visible recipes and attributes of the cookbook with their detail as a json file
   *
   * @param cookbookUrl
   * @param refresh
   * @return
   * @throws KaramelException
   */
  String getCookbookDetails(String cookbookUrl, boolean refresh) throws KaramelException;

  /**
   * Converts json definition of the cluster into a yaml object
   *
   * @param json
   * @return
   * @throws KaramelException
   */
  String jsonToYaml(String json) throws KaramelException;

  /**
   * Converts yaml definition of the cluster into the json
   *
   * @param yaml
   * @return
   * @throws KaramelException
   */
  String yamlToJson(String yaml) throws KaramelException;

  /**
   * Loads Karamel common keys
   *
   * @return
   * @throws KaramelException
   */
  SshKeyPair loadSshKeysIfExist() throws KaramelException;

  /**
   * Loads cluster specific keys
   *
   * @param clusterName
   * @return
   * @throws KaramelException
   */
  SshKeyPair loadSshKeysIfExist(String clusterName) throws KaramelException;

  /**
   * Generates a common ssh keys in the karamel folder
   *
   * @return
   * @throws KaramelException
   */
  SshKeyPair generateSshKeysAndUpdateConf() throws KaramelException;

  /**
   * Generates cluster specific ssh keys
   *
   * @param clusterName
   * @return
   * @throws KaramelException
   */
  SshKeyPair generateSshKeysAndUpdateConf(String clusterName) throws KaramelException;

  /**
   * Register ssh keys for the current runtime of karamel
   *
   * @param keypair
   * @return
   * @throws KaramelException
   */
  SshKeyPair registerSshKeys(SshKeyPair keypair) throws KaramelException;

  /**
   * Register ssh keys for the specified cluster
   *
   * @param clusterName
   * @param keypair
   * @return
   * @throws KaramelException
   */
  SshKeyPair registerSshKeys(String clusterName, SshKeyPair keypair) throws KaramelException;

  /**
   * Reads it from default karamel conf file
   *
   * @return
   * @throws KaramelException
   */
  Ec2Credentials loadEc2CredentialsIfExist() throws KaramelException;

  /**
   * Validates user's credentials before starting the cluster
   *
   * @param credentials
   * @return
   * @throws KaramelException
   */
  boolean updateEc2CredentialsIfValid(Ec2Credentials credentials) throws KaramelException;

  /**
   * Starts running the cluster by launching machines and installing softwares It expect to receive a complete
   * cluster-json for the cluster.
   *
   * @param json
   * @throws KaramelException
   */
  void startCluster(String json) throws KaramelException;

  /**
   * In case user wants to pause the running cluster for inspection reasons. It implies that machines won't receive any
   * new ssh command form the karamel-core. User can either terminate or resume a paused cluster.
   *
   * @param clusterName
   * @throws KaramelException
   */
  void pauseCluster(String clusterName) throws KaramelException;

  /**
   * It resumes an already paused cluster, machines will go on and run ssh commands.
   *
   * @param clusterName
   * @throws KaramelException
   */
  void resumeCluster(String clusterName) throws KaramelException;

  /**
   * It stops sending new ssh command to machines, destroys the automatic allocated machines and disconnects ssh clients
   * from machines. User, however, shouldn't expect that bare-metal machines be destroyed as well.
   *
   * @param clusterName
   * @throws KaramelException
   */
  void terminateCluster(String clusterName) throws KaramelException;

  /**
   * Returns a json containing all groups, machines, their status, tasks/commands and their status.
   *
   * @param clusterName
   * @return
   * @throws KaramelException
   */
  String getClusterStatus(String clusterName) throws KaramelException;

  /**
   * Returns installation flow DAG that each node is a task assigned to a certain machine with the current status of the
   * task.
   *
   * @param clusterName
   * @return
   * @throws KaramelException
   */
  String getInstallationDag(String clusterName) throws KaramelException;

  /**
   * Register password for Baremetal sudo account
   *
   * @param password
   * @throws KaramelException
   */
  void registerSudoPassword(String password) throws KaramelException;

  /**
   * Register username/password for github account
   *
   * @param user github account name
   * @param password github password
   * @return GithubUser Json object also containing primary github email address
   * @throws KaramelException
   */
  GithubUser registerGithubAccount(String user, String password) throws KaramelException;

  /**
   * Load any existing credentials stored locally
   *
   * @return GithubUser object
   * @throws KaramelException
   */
  GithubUser loadGithubCredentials() throws KaramelException;

  /**
   * Lists the available repos in a github organization.
   *
   * @param organization
   * @return List of available repos
   * @throws KaramelException
   */
  List<RepoItem> listGithubRepos(String organization) throws KaramelException;

  /**
   * Lists the available organizations for a user in github. Must call 'registerGithubAccount' first.
   *
   * @return List of available orgs
   * @throws KaramelException
   */
  List<OrgItem> listGithubOrganizations() throws KaramelException;

  /**
   *
   * @param owner
   * @param repo
   * @param removeGitHub
   * @param removeLocal
   */
  void removeRepo(String owner, String repo, boolean removeLocal, boolean removeGitHub) throws KaramelException;

  String loadGceCredentialsIfExist() throws KaramelException;

  boolean updateGceCredentialsIfValid(String jsonFilePath) throws KaramelException;

  NovaCredentials loadNovaCredentialsIfExist() throws KaramelException;

  NovaCredentials loadNovaV3CredentialsIfExist() throws KaramelException;

  boolean updateNovaCredentialsIfValid(NovaCredentials credentials) throws KaramelException;

  boolean updateNovaV3CredentialsIfValid(NovaCredentials credentials) throws KaramelException;

  OcciCredentials loadOcciCredentialsIfExist() throws KaramelException;

  boolean updateOcciCredentialsIfValid(OcciCredentials credentials) throws KaramelException;
}
