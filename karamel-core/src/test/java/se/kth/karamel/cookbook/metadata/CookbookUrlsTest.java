/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import static org.junit.Assert.*;
import org.junit.Test;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class CookbookUrlsTest {

  @Test
  public void testValidUrls() throws CookbookUrlException {
    Settings.CB_CLASSPATH_MODE = false;
    String id = "https://github.com/hopstart/hadoop-chef/tree/master";
    String home = "https://github.com/hopstart/hadoop-chef";
    String repoHome = "https://github.com/hopstart/hadoop-chef";
    String rawHome = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master";
    String metadataFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/metadata.rb";
    String attFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/attributes/default.rb";
    String karamelFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/Karamelfile";
    String berksFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/Berksfile";
    String repo = "hadoop-chef";
    String branch = "master";

    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.url("hopstart/hadoop-chef").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls1.home);
    assertEquals(repoHome, urls1.repoHome);
    assertEquals(rawHome, urls1.rawHome);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);

    CookbookUrls.Builder builder2 = new CookbookUrls.Builder();
    CookbookUrls urls2 = builder2.url("hopstart/hadoop-chef/tree/master").build();
    assertEquals(id, urls2.id);
    assertEquals(home, urls2.home);
    assertEquals(repoHome, urls2.repoHome);
    assertEquals(rawHome, urls2.rawHome);
    assertEquals(metadataFile, urls2.metadataFile);
    assertEquals(attFile, urls2.attrFile);
    assertEquals(karamelFile, urls2.karamelFile);
    assertEquals(berksFile, urls2.berksFile);
    assertEquals(repo, urls2.repoName);
    assertEquals(branch, urls2.branch);

    CookbookUrls.Builder builder3 = new CookbookUrls.Builder();
    CookbookUrls urls3 = builder3.url("https://github.com/hopstart/hadoop-chef").build();
    assertEquals(id, urls3.id);
    assertEquals(home, urls3.home);
    assertEquals(repoHome, urls3.repoHome);
    assertEquals(rawHome, urls3.rawHome);
    assertEquals(metadataFile, urls3.metadataFile);
    assertEquals(attFile, urls3.attrFile);
    assertEquals(karamelFile, urls3.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls3.repoName);
    assertEquals(branch, urls3.branch);

    CookbookUrls.Builder builder4 = new CookbookUrls.Builder();
    CookbookUrls urls4 = builder4.url("https://github.com/hopstart/hadoop-chef/tree/master").build();
    assertEquals(id, urls4.id);
    assertEquals(home, urls4.home);
    assertEquals(repoHome, urls4.repoHome);
    assertEquals(rawHome, urls4.rawHome);
    assertEquals(metadataFile, urls4.metadataFile);
    assertEquals(attFile, urls4.attrFile);
    assertEquals(karamelFile, urls4.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls4.repoName);
    assertEquals(branch, urls4.branch);

    CookbookUrls.Builder builder5 = new CookbookUrls.Builder();
    CookbookUrls urls5 = builder5.url("http://github.com/hopstart/hadoop-chef").build();
    assertEquals(id, urls5.id);
    assertEquals(home, urls5.home);
    assertEquals(repoHome, urls5.repoHome);
    assertEquals(rawHome, urls5.rawHome);
    assertEquals(metadataFile, urls5.metadataFile);
    assertEquals(attFile, urls5.attrFile);
    assertEquals(karamelFile, urls5.karamelFile);
    assertEquals(berksFile, urls5.berksFile);
    assertEquals(repo, urls5.repoName);
    assertEquals(branch, urls5.branch);

    CookbookUrls.Builder builder6 = new CookbookUrls.Builder();
    CookbookUrls urls6 = builder6.url("http://github.com/hopstart/hadoop-chef/tree/master").build();
    assertEquals(id, urls6.id);
    assertEquals(home, urls6.home);
    assertEquals(repoHome, urls6.repoHome);
    assertEquals(rawHome, urls6.rawHome);
    assertEquals(metadataFile, urls6.metadataFile);
    assertEquals(attFile, urls6.attrFile);
    assertEquals(karamelFile, urls6.karamelFile);
    assertEquals(berksFile, urls6.berksFile);
    assertEquals(repo, urls6.repoName);
    assertEquals(branch, urls6.branch);
  }

  @Test
  public void testValidUrlsWithRelativeCookbook() throws CookbookUrlException {
    Settings.CB_CLASSPATH_MODE = false;
    String id = "https://github.com/hopstart/hadoop-chef/tree/master/cookbooks/testcb";
    String home = "https://github.com/hopstart/hadoop-chef/cookbooks/testcb";
    String repoHome = "https://github.com/hopstart/hadoop-chef";
    String rawHome = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/cookbooks/testcb";
    String metadataFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/cookbooks/testcb/metadata.rb";
    String attFile
        = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/cookbooks/testcb/attributes/default.rb";
    String karamelFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/cookbooks/testcb/Karamelfile";
    String berksFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/cookbooks/testcb/Berksfile";
    String repo = "hadoop-chef";
    String branch = "master";

    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.url("hopstart/hadoop-chef").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls1.home);
    assertEquals(repoHome, urls1.repoHome);
    assertEquals(rawHome, urls1.rawHome);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);

    CookbookUrls.Builder builder2 = new CookbookUrls.Builder();
    CookbookUrls urls2 = builder2.url("hopstart/hadoop-chef/tree/master").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls2.id);
    assertEquals(home, urls2.home);
    assertEquals(repoHome, urls2.repoHome);
    assertEquals(rawHome, urls2.rawHome);
    assertEquals(metadataFile, urls2.metadataFile);
    assertEquals(attFile, urls2.attrFile);
    assertEquals(karamelFile, urls2.karamelFile);
    assertEquals(berksFile, urls2.berksFile);
    assertEquals(repo, urls2.repoName);
    assertEquals(branch, urls2.branch);

    CookbookUrls.Builder builder21 = new CookbookUrls.Builder();
    CookbookUrls urls21 = builder21.url("hopstart/hadoop-chef/tree/master/cookbooks/testcb").build();
    assertEquals(id, urls21.id);
    assertEquals(home, urls21.home);
    assertEquals(repoHome, urls21.repoHome);
    assertEquals(rawHome, urls21.rawHome);
    assertEquals(metadataFile, urls21.metadataFile);
    assertEquals(attFile, urls21.attrFile);
    assertEquals(karamelFile, urls21.karamelFile);
    assertEquals(berksFile, urls21.berksFile);
    assertEquals(repo, urls21.repoName);
    assertEquals(branch, urls21.branch);

    CookbookUrls.Builder builder3 = new CookbookUrls.Builder();
    CookbookUrls urls3 = builder3.url("https://github.com/hopstart/hadoop-chef").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls3.home);
    assertEquals(repoHome, urls3.repoHome);
    assertEquals(rawHome, urls3.rawHome);
    assertEquals(metadataFile, urls3.metadataFile);
    assertEquals(attFile, urls3.attrFile);
    assertEquals(karamelFile, urls3.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls3.repoName);
    assertEquals(branch, urls3.branch);

    CookbookUrls.Builder builder4 = new CookbookUrls.Builder();
    CookbookUrls urls4 = builder4.url("https://github.com/hopstart/hadoop-chef/tree/master").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls4.id);
    assertEquals(home, urls4.home);
    assertEquals(repoHome, urls4.repoHome);
    assertEquals(rawHome, urls4.rawHome);
    assertEquals(metadataFile, urls4.metadataFile);
    assertEquals(attFile, urls4.attrFile);
    assertEquals(karamelFile, urls4.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls4.repoName);
    assertEquals(branch, urls4.branch);

    CookbookUrls.Builder builder5 = new CookbookUrls.Builder();
    CookbookUrls urls5 = builder5.url("http://github.com/hopstart/hadoop-chef").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls5.id);
    assertEquals(home, urls5.home);
    assertEquals(repoHome, urls5.repoHome);
    assertEquals(rawHome, urls5.rawHome);
    assertEquals(metadataFile, urls5.metadataFile);
    assertEquals(attFile, urls5.attrFile);
    assertEquals(karamelFile, urls5.karamelFile);
    assertEquals(berksFile, urls5.berksFile);
    assertEquals(repo, urls5.repoName);
    assertEquals(branch, urls5.branch);

    CookbookUrls.Builder builder6 = new CookbookUrls.Builder();
    CookbookUrls urls6 = builder6.url("http://github.com/hopstart/hadoop-chef/tree/master").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls6.id);
    assertEquals(home, urls6.home);
    assertEquals(repoHome, urls6.repoHome);
    assertEquals(rawHome, urls6.rawHome);
    assertEquals(metadataFile, urls6.metadataFile);
    assertEquals(attFile, urls6.attrFile);
    assertEquals(karamelFile, urls6.karamelFile);
    assertEquals(berksFile, urls6.berksFile);
    assertEquals(repo, urls6.repoName);
    assertEquals(branch, urls6.branch);

    CookbookUrls.Builder builder7 = new CookbookUrls.Builder();
    CookbookUrls urls7 = builder7.url("http://github.com/hopstart/hadoop-chef/tree/master/cookbooks/testcb").build();
    assertEquals(id, urls7.id);
    assertEquals(home, urls7.home);
    assertEquals(repoHome, urls7.repoHome);
    assertEquals(rawHome, urls7.rawHome);
    assertEquals(metadataFile, urls7.metadataFile);
    assertEquals(attFile, urls7.attrFile);
    assertEquals(karamelFile, urls7.karamelFile);
    assertEquals(berksFile, urls7.berksFile);
    assertEquals(repo, urls7.repoName);
    assertEquals(branch, urls7.branch);
  }

  @Test
  public void testValidUrlsWithVersion() throws CookbookUrlException {
    Settings.CB_CLASSPATH_MODE = false;
    String id = "https://github.com/hopstart/hadoop-chef/tree/v0.1";
    String home = "https://github.com/hopstart/hadoop-chef";
    String repoHome = "https://github.com/hopstart/hadoop-chef";
    String rawHome = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1";
    String metadataFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/metadata.rb";
    String attFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/attributes/default.rb";
    String karamelFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/Karamelfile";
    String berksFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/Berksfile";
    String repo = "hadoop-chef";
    String branch = "v0.1";

    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.url("hopstart/hadoop-chef").branchOrVersion("v0.1").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls1.home);
    assertEquals(repoHome, urls1.repoHome);
    assertEquals(rawHome, urls1.rawHome);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);

    CookbookUrls.Builder builder2 = new CookbookUrls.Builder();
    CookbookUrls urls2 = builder2.url("hopstart/hadoop-chef/tree/master").branchOrVersion("v0.1").build();
    assertEquals(id, urls2.id);
    assertEquals(home, urls2.home);
    assertEquals(repoHome, urls2.repoHome);
    assertEquals(rawHome, urls2.rawHome);
    assertEquals(metadataFile, urls2.metadataFile);
    assertEquals(attFile, urls2.attrFile);
    assertEquals(karamelFile, urls2.karamelFile);
    assertEquals(berksFile, urls2.berksFile);
    assertEquals(repo, urls2.repoName);
    assertEquals(branch, urls2.branch);

    CookbookUrls.Builder builder3 = new CookbookUrls.Builder();
    CookbookUrls urls3 = builder3.url("https://github.com/hopstart/hadoop-chef").branchOrVersion("v0.1").build();
    assertEquals(id, urls3.id);
    assertEquals(home, urls3.home);
    assertEquals(rawHome, urls3.rawHome);
    assertEquals(repoHome, urls3.repoHome);
    assertEquals(metadataFile, urls3.metadataFile);
    assertEquals(attFile, urls3.attrFile);
    assertEquals(karamelFile, urls3.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls3.repoName);
    assertEquals(branch, urls3.branch);

    CookbookUrls.Builder builder4 = new CookbookUrls.Builder();
    CookbookUrls urls4 = builder4.
        url("https://github.com/hopstart/hadoop-chef/tree/master").branchOrVersion("v0.1").build();
    assertEquals(id, urls4.id);
    assertEquals(home, urls4.home);
    assertEquals(repoHome, urls4.repoHome);
    assertEquals(rawHome, urls4.rawHome);
    assertEquals(metadataFile, urls4.metadataFile);
    assertEquals(attFile, urls4.attrFile);
    assertEquals(karamelFile, urls4.karamelFile);
    assertEquals(berksFile, urls4.berksFile);
    assertEquals(repo, urls4.repoName);
    assertEquals(branch, urls4.branch);

    CookbookUrls.Builder builder5 = new CookbookUrls.Builder();
    CookbookUrls urls5 = builder5.url("http://github.com/hopstart/hadoop-chef").branchOrVersion("v0.1").build();
    assertEquals(id, urls5.id);
    assertEquals(home, urls5.home);
    assertEquals(repoHome, urls5.repoHome);
    assertEquals(rawHome, urls5.rawHome);
    assertEquals(metadataFile, urls5.metadataFile);
    assertEquals(attFile, urls5.attrFile);
    assertEquals(karamelFile, urls5.karamelFile);
    assertEquals(berksFile, urls5.berksFile);
    assertEquals(repo, urls5.repoName);
    assertEquals(branch, urls5.branch);

    CookbookUrls.Builder builder6 = new CookbookUrls.Builder();
    CookbookUrls urls6 = builder6.
        url("http://github.com/hopstart/hadoop-chef/tree/master").branchOrVersion("v0.1").build();
    assertEquals(id, urls6.id);
    assertEquals(home, urls6.home);
    assertEquals(repoHome, urls6.repoHome);
    assertEquals(rawHome, urls6.rawHome);
    assertEquals(metadataFile, urls6.metadataFile);
    assertEquals(berksFile, urls6.berksFile);
    assertEquals(attFile, urls6.attrFile);
    assertEquals(karamelFile, urls6.karamelFile);
  }

  @Test
  public void testValidUrlsWithVersionAndRelativeCookbook() throws CookbookUrlException {
    Settings.CB_CLASSPATH_MODE = false;
    String id = "https://github.com/hopstart/hadoop-chef/tree/v0.1/cookbooks/testcb";
    String home = "https://github.com/hopstart/hadoop-chef/cookbooks/testcb";
    String repoHome = "https://github.com/hopstart/hadoop-chef";
    String rawHome = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/cookbooks/testcb";
    String metadataFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/cookbooks/testcb/metadata.rb";
    String attFile
        = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/cookbooks/testcb/attributes/default.rb";
    String karamelFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/cookbooks/testcb/Karamelfile";
    String berksFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/cookbooks/testcb/Berksfile";
    String repo = "hadoop-chef";
    String branch = "v0.1";

    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.
        url("hopstart/hadoop-chef").branchOrVersion("v0.1").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls1.home);
    assertEquals(repoHome, urls1.repoHome);
    assertEquals(rawHome, urls1.rawHome);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);

    CookbookUrls.Builder builder2 = new CookbookUrls.Builder();
    CookbookUrls urls2 = builder2.
        url("hopstart/hadoop-chef/tree/master").branchOrVersion("v0.1").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls2.id);
    assertEquals(home, urls2.home);
    assertEquals(repoHome, urls2.repoHome);
    assertEquals(rawHome, urls2.rawHome);
    assertEquals(metadataFile, urls2.metadataFile);
    assertEquals(attFile, urls2.attrFile);
    assertEquals(karamelFile, urls2.karamelFile);
    assertEquals(berksFile, urls2.berksFile);
    assertEquals(repo, urls2.repoName);
    assertEquals(branch, urls2.branch);

    CookbookUrls.Builder builder3 = new CookbookUrls.Builder();
    CookbookUrls urls3 = builder3.
        url("https://github.com/hopstart/hadoop-chef").branchOrVersion("v0.1").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls3.id);
    assertEquals(home, urls3.home);
    assertEquals(repoHome, urls3.repoHome);
    assertEquals(rawHome, urls3.rawHome);
    assertEquals(metadataFile, urls3.metadataFile);
    assertEquals(attFile, urls3.attrFile);
    assertEquals(karamelFile, urls3.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls3.repoName);
    assertEquals(branch, urls3.branch);

    CookbookUrls.Builder builder4 = new CookbookUrls.Builder();
    CookbookUrls urls4 = builder4.url("https://github.com/hopstart/hadoop-chef/tree/master").
        branchOrVersion("v0.1").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls4.id);
    assertEquals(home, urls4.home);
    assertEquals(repoHome, urls4.repoHome);
    assertEquals(rawHome, urls4.rawHome);
    assertEquals(metadataFile, urls4.metadataFile);
    assertEquals(attFile, urls4.attrFile);
    assertEquals(karamelFile, urls4.karamelFile);
    assertEquals(berksFile, urls4.berksFile);
    assertEquals(repo, urls4.repoName);
    assertEquals(branch, urls4.branch);

    CookbookUrls.Builder builder5 = new CookbookUrls.Builder();
    CookbookUrls urls5 = builder5.url("http://github.com/hopstart/hadoop-chef").
        branchOrVersion("v0.1").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls5.id);
    assertEquals(home, urls5.home);
    assertEquals(repoHome, urls5.repoHome);
    assertEquals(rawHome, urls5.rawHome);
    assertEquals(metadataFile, urls5.metadataFile);
    assertEquals(attFile, urls5.attrFile);
    assertEquals(karamelFile, urls5.karamelFile);
    assertEquals(berksFile, urls5.berksFile);
    assertEquals(repo, urls5.repoName);
    assertEquals(branch, urls5.branch);

    CookbookUrls.Builder builder6 = new CookbookUrls.Builder();
    CookbookUrls urls6 = builder6.url("http://github.com/hopstart/hadoop-chef/tree/master").
        branchOrVersion("v0.1").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls6.id);
    assertEquals(home, urls6.home);
    assertEquals(repoHome, urls6.repoHome);
    assertEquals(rawHome, urls6.rawHome);
    assertEquals(metadataFile, urls6.metadataFile);
    assertEquals(berksFile, urls6.berksFile);
    assertEquals(attFile, urls6.attrFile);
    assertEquals(karamelFile, urls6.karamelFile);
  }

  @Test
  public void testValidUrlsWithVersionInClasspath() throws CookbookUrlException {
    String id = "https://github.com/hopshadoop/hopsworks-chef/tree/master";
    String home = "testgithub/hopshadoop/hopsworks-chef";
    String rawHome = "testgithub/hopshadoop/hopsworks-chef/master";
    String repoHome = "testgithub/hopshadoop/hopsworks-chef";
    String metadataFile = "testgithub/hopshadoop/hopsworks-chef/master/metadata.rb";
    String attFile = "testgithub/hopshadoop/hopsworks-chef/master/attributes/default.rb";
    String karamelFile = "testgithub/hopshadoop/hopsworks-chef/master/Karamelfile";
    String berksFile = "testgithub/hopshadoop/hopsworks-chef/master/Berksfile";
    String repo = "hopsworks-chef";
    String branch = "master";
    Settings.CB_CLASSPATH_MODE = true;
    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.url("hopshadoop/hopsworks-chef").branchOrVersion("master").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls1.home);
    assertEquals(repoHome, urls1.repoHome);
    assertEquals(rawHome, urls1.rawHome);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);
  }

  @Test
  public void testValidUrlsWithVersionAndRekativeCookbookInClasspath() throws CookbookUrlException {
    String id = "https://github.com/hopshadoop/hopshub-chef/tree/master/cookbooks/testcb";
    String home = "testgithub/hopshadoop/hopshub-chef/cookbooks/testcb";
    String repoHome = "testgithub/hopshadoop/hopshub-chef";
    String rawHome = "testgithub/hopshadoop/hopshub-chef/master/cookbooks/testcb";
    String metadataFile = "testgithub/hopshadoop/hopshub-chef/master/cookbooks/testcb/metadata.rb";
    String attFile = "testgithub/hopshadoop/hopshub-chef/master/cookbooks/testcb/attributes/default.rb";
    String karamelFile = "testgithub/hopshadoop/hopshub-chef/master/cookbooks/testcb/Karamelfile";
    String berksFile = "testgithub/hopshadoop/hopshub-chef/master/cookbooks/testcb/Berksfile";
    String repo = "hopshub-chef";
    String branch = "master";
    Settings.CB_CLASSPATH_MODE = true;
    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.url("hopshadoop/hopshub-chef").
        branchOrVersion("master").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls1.home);
    assertEquals(repoHome, urls1.repoHome);
    assertEquals(rawHome, urls1.rawHome);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);
  }

}
