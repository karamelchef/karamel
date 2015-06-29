/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.api;

import java.io.IOException;
import org.jclouds.domain.Credentials;
import se.kth.karamel.backend.command.CommandResponse;
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
   * @param email github account name or email address
   * @param password github password
   * @throws KaramelException
   */
  public void registerGithubAccount(String email, String password) throws KaramelException;

  public String loadGceCredentialsIfExist() throws KaramelException;

  public boolean updateGceCredentialsIfValid(String jsonFilePath) throws KaramelException;
}
