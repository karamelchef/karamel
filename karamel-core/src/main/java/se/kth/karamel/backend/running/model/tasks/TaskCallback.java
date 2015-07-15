/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.backend.running.model.tasks;

/**
 * A callback that Task gives to TaskSubmitter to receive updates about execution status. 
 * 
 * @author kamal
 */
public interface TaskCallback {
  public void queued();
  public void started();
  public void succeed();
  public void failed(String reason);
}
