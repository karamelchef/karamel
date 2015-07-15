/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.dag;

/**
 * A callback that is set by DagNode into Task at the time of submit to receive status update. 
 * 
 * @author kamal
 */
public interface DagTaskCallback {

  public void queued();

  public void started();

  public void succeed();

  public void failed(String reason);
}
