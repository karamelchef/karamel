/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.client.api;

import se.kth.karamel.common.exception.KaramelException;

/**
 * The main API of Karamel-Core for Karamel clients
 * 
 * @author kamal
 */
public interface KaramelApi {
 
  /**
   * Returns visible recipes and attributes of the cookbook
   * with their detail as a json file
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
   * Validates user's credentials before starting the cluster
   * 
   * @param account
   * @param accessKey
   * @return
   * @throws KaramelException 
   */
  public boolean updateEc2CredentialsIfValid(String account, String accessKey) throws KaramelException;
  
  /**
   * Starts running the cluster by launching machines and installing softwares 
   * It expect to receive a complete cluster-json for the cluster. 
   * 
   * @param json
   * @throws KaramelException 
   */
  public void startCluster(String json)  throws KaramelException;
  
  /**
   * In case user wants to pause the running cluster for inspection reasons. 
   * It implies that machines won't receive any new ssh command form the karamel-core. 
   * User can either purge or resume a paused cluster.
   * @param clusterName
   * @throws KaramelException 
   */
  public void pauseCluster(String clusterName) throws KaramelException;
  
  /**
   * It resumes an already paused cluster, machines will go on and run ssh commands.
   * @param clusterName
   * @throws KaramelException 
   */
  public void resumeCluster(String clusterName) throws KaramelException;
  
  /**
   * It stops sending new ssh command to machines, destroys the automatic allocated machines and disconnects ssh clients
   * from machines. User, however, shouldn't expect that bare-metal machines be destroyed as well.
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
  public String getClusterStatus(String clusterName)  throws KaramelException;
  
  /**
   * Returns installation flow DAG that each node is a task assigned to a certain machine with the current status of the
   * task.
   * @param clusterName
   * @return
   * @throws KaramelException 
   */
  public String getInstallationDag(String clusterName)  throws KaramelException;
  
}
