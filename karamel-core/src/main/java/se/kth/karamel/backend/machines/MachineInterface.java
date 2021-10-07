/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.machines;

import java.io.IOException;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public interface MachineInterface {

  public void downloadRemoteFile(String remoteFilePath, String localFilePath, boolean overwrite)
      throws KaramelException, IOException;

}
