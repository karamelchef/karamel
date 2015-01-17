/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.common;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;

/**
 *
 * @author kamal
 */
public class ClasspathResourceUtil {
  
  public static String readContent(String path) throws IOException {
    return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
  }
}
