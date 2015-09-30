/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import se.kth.karamel.common.cookbookmeta.CookbookUrls;
import static org.junit.Assert.*;
import org.junit.Test;
import se.kth.karamel.common.util.Settings;
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
    String cookbookUrl = "https://github.com/hopstart/hadoop-chef";
    String repoUrl = "https://github.com/hopstart/hadoop-chef";
    String cookbookRawUrl = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master";
    String metadataFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/metadata.rb";
    String attFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/attributes/default.rb";
    String karamelFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/Karamelfile";
    String berksFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/Berksfile";
    String repo = "hadoop-chef";
    String branch = "master";

    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.url("hopstart/hadoop-chef").build();
    assertEquals(id, urls1.id);
    assertEquals(cookbookUrl, urls1.cookbookUrl);
    assertEquals(repoUrl, urls1.repoUrl);
    assertEquals(cookbookRawUrl, urls1.cookbookRawUrl);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);

    CookbookUrls.Builder builder2 = new CookbookUrls.Builder();
    CookbookUrls urls2 = builder2.url("hopstart/hadoop-chef/tree/master").build();
    assertEquals(id, urls2.id);
    assertEquals(cookbookUrl, urls2.cookbookUrl);
    assertEquals(repoUrl, urls2.repoUrl);
    assertEquals(cookbookRawUrl, urls2.cookbookRawUrl);
    assertEquals(metadataFile, urls2.metadataFile);
    assertEquals(attFile, urls2.attrFile);
    assertEquals(karamelFile, urls2.karamelFile);
    assertEquals(berksFile, urls2.berksFile);
    assertEquals(repo, urls2.repoName);
    assertEquals(branch, urls2.branch);

    CookbookUrls.Builder builder3 = new CookbookUrls.Builder();
    CookbookUrls urls3 = builder3.url("https://github.com/hopstart/hadoop-chef").build();
    assertEquals(id, urls3.id);
    assertEquals(cookbookUrl, urls3.cookbookUrl);
    assertEquals(repoUrl, urls3.repoUrl);
    assertEquals(cookbookRawUrl, urls3.cookbookRawUrl);
    assertEquals(metadataFile, urls3.metadataFile);
    assertEquals(attFile, urls3.attrFile);
    assertEquals(karamelFile, urls3.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls3.repoName);
    assertEquals(branch, urls3.branch);

    CookbookUrls.Builder builder4 = new CookbookUrls.Builder();
    CookbookUrls urls4 = builder4.url("https://github.com/hopstart/hadoop-chef/tree/master").build();
    assertEquals(id, urls4.id);
    assertEquals(cookbookUrl, urls4.cookbookUrl);
    assertEquals(repoUrl, urls4.repoUrl);
    assertEquals(cookbookRawUrl, urls4.cookbookRawUrl);
    assertEquals(metadataFile, urls4.metadataFile);
    assertEquals(attFile, urls4.attrFile);
    assertEquals(karamelFile, urls4.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls4.repoName);
    assertEquals(branch, urls4.branch);

    CookbookUrls.Builder builder5 = new CookbookUrls.Builder();
    CookbookUrls urls5 = builder5.url("http://github.com/hopstart/hadoop-chef").build();
    assertEquals(id, urls5.id);
    assertEquals(cookbookUrl, urls5.cookbookUrl);
    assertEquals(repoUrl, urls5.repoUrl);
    assertEquals(cookbookRawUrl, urls5.cookbookRawUrl);
    assertEquals(metadataFile, urls5.metadataFile);
    assertEquals(attFile, urls5.attrFile);
    assertEquals(karamelFile, urls5.karamelFile);
    assertEquals(berksFile, urls5.berksFile);
    assertEquals(repo, urls5.repoName);
    assertEquals(branch, urls5.branch);

    CookbookUrls.Builder builder6 = new CookbookUrls.Builder();
    CookbookUrls urls6 = builder6.url("http://github.com/hopstart/hadoop-chef/tree/master").build();
    assertEquals(id, urls6.id);
    assertEquals(cookbookUrl, urls6.cookbookUrl);
    assertEquals(repoUrl, urls6.repoUrl);
    assertEquals(cookbookRawUrl, urls6.cookbookRawUrl);
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
    String cookbookUrl = "https://github.com/hopstart/hadoop-chef/cookbooks/testcb";
    String repoUrl = "https://github.com/hopstart/hadoop-chef";
    String cookbookRawUrl = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/cookbooks/testcb";
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
    assertEquals(cookbookUrl, urls1.cookbookUrl);
    assertEquals(repoUrl, urls1.repoUrl);
    assertEquals(cookbookRawUrl, urls1.cookbookRawUrl);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);

    CookbookUrls.Builder builder2 = new CookbookUrls.Builder();
    CookbookUrls urls2 = builder2.url("hopstart/hadoop-chef/tree/master").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls2.id);
    assertEquals(cookbookUrl, urls2.cookbookUrl);
    assertEquals(repoUrl, urls2.repoUrl);
    assertEquals(cookbookRawUrl, urls2.cookbookRawUrl);
    assertEquals(metadataFile, urls2.metadataFile);
    assertEquals(attFile, urls2.attrFile);
    assertEquals(karamelFile, urls2.karamelFile);
    assertEquals(berksFile, urls2.berksFile);
    assertEquals(repo, urls2.repoName);
    assertEquals(branch, urls2.branch);

    CookbookUrls.Builder builder21 = new CookbookUrls.Builder();
    CookbookUrls urls21 = builder21.url("hopstart/hadoop-chef/tree/master/cookbooks/testcb").build();
    assertEquals(id, urls21.id);
    assertEquals(cookbookUrl, urls21.cookbookUrl);
    assertEquals(repoUrl, urls21.repoUrl);
    assertEquals(cookbookRawUrl, urls21.cookbookRawUrl);
    assertEquals(metadataFile, urls21.metadataFile);
    assertEquals(attFile, urls21.attrFile);
    assertEquals(karamelFile, urls21.karamelFile);
    assertEquals(berksFile, urls21.berksFile);
    assertEquals(repo, urls21.repoName);
    assertEquals(branch, urls21.branch);

    CookbookUrls.Builder builder3 = new CookbookUrls.Builder();
    CookbookUrls urls3 = builder3.url("https://github.com/hopstart/hadoop-chef").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls1.id);
    assertEquals(cookbookUrl, urls3.cookbookUrl);
    assertEquals(repoUrl, urls3.repoUrl);
    assertEquals(cookbookRawUrl, urls3.cookbookRawUrl);
    assertEquals(metadataFile, urls3.metadataFile);
    assertEquals(attFile, urls3.attrFile);
    assertEquals(karamelFile, urls3.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls3.repoName);
    assertEquals(branch, urls3.branch);

    CookbookUrls.Builder builder4 = new CookbookUrls.Builder();
    CookbookUrls urls4 = builder4.url("https://github.com/hopstart/hadoop-chef/tree/master").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls4.id);
    assertEquals(cookbookUrl, urls4.cookbookUrl);
    assertEquals(repoUrl, urls4.repoUrl);
    assertEquals(cookbookRawUrl, urls4.cookbookRawUrl);
    assertEquals(metadataFile, urls4.metadataFile);
    assertEquals(attFile, urls4.attrFile);
    assertEquals(karamelFile, urls4.karamelFile);
    assertEquals(berksFile, urls3.berksFile);
    assertEquals(repo, urls4.repoName);
    assertEquals(branch, urls4.branch);

    CookbookUrls.Builder builder5 = new CookbookUrls.Builder();
    CookbookUrls urls5 = builder5.url("http://github.com/hopstart/hadoop-chef").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls5.id);
    assertEquals(cookbookUrl, urls5.cookbookUrl);
    assertEquals(repoUrl, urls5.repoUrl);
    assertEquals(cookbookRawUrl, urls5.cookbookRawUrl);
    assertEquals(metadataFile, urls5.metadataFile);
    assertEquals(attFile, urls5.attrFile);
    assertEquals(karamelFile, urls5.karamelFile);
    assertEquals(berksFile, urls5.berksFile);
    assertEquals(repo, urls5.repoName);
    assertEquals(branch, urls5.branch);

    CookbookUrls.Builder builder6 = new CookbookUrls.Builder();
    CookbookUrls urls6 = builder6.url("http://github.com/hopstart/hadoop-chef/tree/master").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls6.id);
    assertEquals(cookbookUrl, urls6.cookbookUrl);
    assertEquals(repoUrl, urls6.repoUrl);
    assertEquals(cookbookRawUrl, urls6.cookbookRawUrl);
    assertEquals(metadataFile, urls6.metadataFile);
    assertEquals(attFile, urls6.attrFile);
    assertEquals(karamelFile, urls6.karamelFile);
    assertEquals(berksFile, urls6.berksFile);
    assertEquals(repo, urls6.repoName);
    assertEquals(branch, urls6.branch);

    CookbookUrls.Builder builder7 = new CookbookUrls.Builder();
    CookbookUrls urls7 = builder7.url("http://github.com/hopstart/hadoop-chef/tree/master/cookbooks/testcb").build();
    assertEquals(id, urls7.id);
    assertEquals(cookbookUrl, urls7.cookbookUrl);
    assertEquals(repoUrl, urls7.repoUrl);
    assertEquals(cookbookRawUrl, urls7.cookbookRawUrl);
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
    String cookbookUrl = "https://github.com/hopstart/hadoop-chef";
    String repoUrl = "https://github.com/hopstart/hadoop-chef";
    String cookbookRawUrl = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1";
    String metadataFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/metadata.rb";
    String attFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/attributes/default.rb";
    String karamelFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/Karamelfile";
    String berksFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/Berksfile";
    String repo = "hadoop-chef";
    String branch = "v0.1";

    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.url("hopstart/hadoop-chef").branchOrVersion("v0.1").build();
    assertEquals(id, urls1.id);
    assertEquals(cookbookUrl, urls1.cookbookUrl);
    assertEquals(repoUrl, urls1.repoUrl);
    assertEquals(cookbookRawUrl, urls1.cookbookRawUrl);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);

    CookbookUrls.Builder builder2 = new CookbookUrls.Builder();
    CookbookUrls urls2 = builder2.url("hopstart/hadoop-chef/tree/master").branchOrVersion("v0.1").build();
    assertEquals(id, urls2.id);
    assertEquals(cookbookUrl, urls2.cookbookUrl);
    assertEquals(repoUrl, urls2.repoUrl);
    assertEquals(cookbookRawUrl, urls2.cookbookRawUrl);
    assertEquals(metadataFile, urls2.metadataFile);
    assertEquals(attFile, urls2.attrFile);
    assertEquals(karamelFile, urls2.karamelFile);
    assertEquals(berksFile, urls2.berksFile);
    assertEquals(repo, urls2.repoName);
    assertEquals(branch, urls2.branch);

    CookbookUrls.Builder builder3 = new CookbookUrls.Builder();
    CookbookUrls urls3 = builder3.url("https://github.com/hopstart/hadoop-chef").branchOrVersion("v0.1").build();
    assertEquals(id, urls3.id);
    assertEquals(cookbookUrl, urls3.cookbookUrl);
    assertEquals(cookbookRawUrl, urls3.cookbookRawUrl);
    assertEquals(repoUrl, urls3.repoUrl);
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
    assertEquals(cookbookUrl, urls4.cookbookUrl);
    assertEquals(repoUrl, urls4.repoUrl);
    assertEquals(cookbookRawUrl, urls4.cookbookRawUrl);
    assertEquals(metadataFile, urls4.metadataFile);
    assertEquals(attFile, urls4.attrFile);
    assertEquals(karamelFile, urls4.karamelFile);
    assertEquals(berksFile, urls4.berksFile);
    assertEquals(repo, urls4.repoName);
    assertEquals(branch, urls4.branch);

    CookbookUrls.Builder builder5 = new CookbookUrls.Builder();
    CookbookUrls urls5 = builder5.url("http://github.com/hopstart/hadoop-chef").branchOrVersion("v0.1").build();
    assertEquals(id, urls5.id);
    assertEquals(cookbookUrl, urls5.cookbookUrl);
    assertEquals(repoUrl, urls5.repoUrl);
    assertEquals(cookbookRawUrl, urls5.cookbookRawUrl);
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
    assertEquals(cookbookUrl, urls6.cookbookUrl);
    assertEquals(repoUrl, urls6.repoUrl);
    assertEquals(cookbookRawUrl, urls6.cookbookRawUrl);
    assertEquals(metadataFile, urls6.metadataFile);
    assertEquals(berksFile, urls6.berksFile);
    assertEquals(attFile, urls6.attrFile);
    assertEquals(karamelFile, urls6.karamelFile);
  }

  @Test
  public void testValidUrlsWithVersionAndRelativeCookbook() throws CookbookUrlException {
    Settings.CB_CLASSPATH_MODE = false;
    String id = "https://github.com/hopstart/hadoop-chef/tree/v0.1/cookbooks/testcb";
    String cookbookUrl = "https://github.com/hopstart/hadoop-chef/cookbooks/testcb";
    String repoUrl = "https://github.com/hopstart/hadoop-chef";
    String cookbookRawUrl = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/cookbooks/testcb";
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
    assertEquals(cookbookUrl, urls1.cookbookUrl);
    assertEquals(repoUrl, urls1.repoUrl);
    assertEquals(cookbookRawUrl, urls1.cookbookRawUrl);
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
    assertEquals(cookbookUrl, urls2.cookbookUrl);
    assertEquals(repoUrl, urls2.repoUrl);
    assertEquals(cookbookRawUrl, urls2.cookbookRawUrl);
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
    assertEquals(cookbookUrl, urls3.cookbookUrl);
    assertEquals(repoUrl, urls3.repoUrl);
    assertEquals(cookbookRawUrl, urls3.cookbookRawUrl);
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
    assertEquals(cookbookUrl, urls4.cookbookUrl);
    assertEquals(repoUrl, urls4.repoUrl);
    assertEquals(cookbookRawUrl, urls4.cookbookRawUrl);
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
    assertEquals(cookbookUrl, urls5.cookbookUrl);
    assertEquals(repoUrl, urls5.repoUrl);
    assertEquals(cookbookRawUrl, urls5.cookbookRawUrl);
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
    assertEquals(cookbookUrl, urls6.cookbookUrl);
    assertEquals(repoUrl, urls6.repoUrl);
    assertEquals(cookbookRawUrl, urls6.cookbookRawUrl);
    assertEquals(metadataFile, urls6.metadataFile);
    assertEquals(berksFile, urls6.berksFile);
    assertEquals(attFile, urls6.attrFile);
    assertEquals(karamelFile, urls6.karamelFile);
  }

  @Test
  public void testValidUrlsWithVersionInClasspath() throws CookbookUrlException {
    String id = "https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/hopsworks-chef";
    String cookbookUrl = "testgithub/testorg/testrepo/cookbooks/hopshadoop/hopsworks-chef";
    String cookbookRawUrl = "testgithub/testorg/testrepo/master/cookbooks/hopshadoop/hopsworks-chef";
    String repoUrl = "testgithub/testorg/testrepo";
    String metadataFile = "testgithub/testorg/testrepo/master/cookbooks/hopshadoop/hopsworks-chef/metadata.rb";
    String attFile = "testgithub/testorg/testrepo/master/cookbooks/hopshadoop/hopsworks-chef/attributes/default.rb";
    String karamelFile = "testgithub/testorg/testrepo/master/cookbooks/hopshadoop/hopsworks-chef/Karamelfile";
    String berksFile = "testgithub/testorg/testrepo/master/cookbooks/hopshadoop/hopsworks-chef/Berksfile";
    String repo = "testrepo";
    String branch = "master";
    Settings.CB_CLASSPATH_MODE = true;
    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.url("testorg/testrepo/tree/master/cookbooks/hopshadoop/hopsworks-chef").branchOrVersion("master").build();
    assertEquals(id, urls1.id);
    assertEquals(cookbookUrl, urls1.cookbookUrl);
    assertEquals(repoUrl, urls1.repoUrl);
    assertEquals(cookbookRawUrl, urls1.cookbookRawUrl);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);
  }

  @Test
  public void testValidUrlsWithVersionAndRekativeCookbookInClasspath() throws CookbookUrlException {
    String id = "https://github.com/testorg/testrepo/tree/master/cookbooks/testcb";
    String cookbookUrl = "testgithub/testorg/testrepo/cookbooks/testcb";
    String repoUrl = "testgithub/testorg/testrepo";
    String cookbookRawUrl = "testgithub/testorg/testrepo/master/cookbooks/testcb";
    String metadataFile = "testgithub/testorg/testrepo/master/cookbooks/testcb/metadata.rb";
    String attFile = "testgithub/testorg/testrepo/master/cookbooks/testcb/attributes/default.rb";
    String karamelFile = "testgithub/testorg/testrepo/master/cookbooks/testcb/Karamelfile";
    String berksFile = "testgithub/testorg/testrepo/master/cookbooks/testcb/Berksfile";
    String orgRepo = "testorg/testrepo";
    String repo = "testrepo";
    String branch = "master";
    Settings.CB_CLASSPATH_MODE = true;
    CookbookUrls.Builder builder1 = new CookbookUrls.Builder();
    CookbookUrls urls1 = builder1.url("testorg/testrepo").
        branchOrVersion("master").cookbookRelPath("cookbooks/testcb").build();
    assertEquals(id, urls1.id);
    assertEquals(cookbookUrl, urls1.cookbookUrl);
    assertEquals(repoUrl, urls1.repoUrl);
    assertEquals(cookbookRawUrl, urls1.cookbookRawUrl);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(berksFile, urls1.berksFile);
    assertEquals(orgRepo, urls1.orgRepo);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);
  }

}
