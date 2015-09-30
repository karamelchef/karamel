/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.util;

import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author kamal
 */
public class FilesystemUtil {

  public static boolean deleteRecursive(String path) throws FileNotFoundException {
    File file = new File(path);
    return deleteRecursive(file);
  }

  /**
   * By default File#delete fails for non-empty directories, it works like "rm". We need something a little more brutual
   * - this does the equivalent of "rm -r"
   *
   * @param path Root File Path
   * @return true iff the file and all sub files/directories have been removed
   * @throws FileNotFoundException
   */
  public static boolean deleteRecursive(File path) throws FileNotFoundException {
    if (!path.exists()) {
      throw new FileNotFoundException(path.getAbsolutePath());
    }
    boolean ret = true;
    if (path.isDirectory()) {
      for (File f : path.listFiles()) {
        ret = ret && deleteRecursive(f);
      }
    }
    return ret && path.delete();
  }
}
