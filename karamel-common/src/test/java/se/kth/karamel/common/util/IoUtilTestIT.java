/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import org.junit.Test;

/**
 *
 * @author kamal
 */
public class IoUtilTestIT {

  @Test
  public void testduration() throws IOException {
    Set<String> paths = new HashSet<>();
    paths.add("https://raw.githubusercontent.com/hopshadoop/apache-hadoop-chef/master/Karamelfile");
    paths.add("https://raw.githubusercontent.com/hopshadoop/apache-hadoop-chef/master/metadata.rb");
    paths.add("https://raw.githubusercontent.com/hopshadoop/apache-hadoop-chef/master/attributes/default.rb");
    paths.add("https://raw.githubusercontent.com/hopshadoop/apache-hadoop-chef/master/Berksfile");
    
    paths.add("https://raw.githubusercontent.com/hopshadoop/apache-hadoop-chef/master/README.md");
    paths.add("https://raw.githubusercontent.com/hopshadoop/apache-hadoop-chef/master/Rakefile");
    paths.add("https://raw.githubusercontent.com/hopshadoop/apache-hadoop-chef/master/wrongurl");

    long start = System.currentTimeMillis();
    Map<String, String> map = IoUtils.readContentParallel(paths, Executors.newFixedThreadPool(7));
    long end = System.currentTimeMillis();
    System.out.println("it took " + ((end - start)/1000) + "s for " + paths.size() + " files, it returned " 
        + map.size() + " contet");
  }
}
