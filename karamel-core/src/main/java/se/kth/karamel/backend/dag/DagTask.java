/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.dag;

import java.util.Set;

/**
 * An interface of Task to DagNode. 
 * 
 * @author kamal
 */
public interface DagTask {

  public String dagNodeId();

  public void prepareToStart();

  public void terminate();
  
  public void submit(DagTaskCallback callback);

  public Set<String> dagDependencies();
  
  public String asJson();
}
