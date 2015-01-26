/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.cookbook.metadata;

import org.junit.Test;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class BerksfileTest {
  
  @Test
  public void testLoadDependencies() throws CookbookUrlException {
    Berksfile berksfile = new Berksfile("https://raw.githubusercontent.com/kahak/hiway-chef/master/Berksfile");
  }
}
