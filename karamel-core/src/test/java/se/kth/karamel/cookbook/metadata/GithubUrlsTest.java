/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import static org.junit.Assert.*;
import org.junit.Test;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class GithubUrlsTest {

  @Test
  public void testValidUrls() throws CookbookUrlException {
    String id = "https://github.com/hopstart/hadoop-chef/tree/master";
    String home = "https://github.com/hopstart/hadoop-chef";
    String rawHome = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master";
    String metadataFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/metadata.rb";
    String attFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/attributes/default.rb";
    String karamelFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/master/Karamelfile";
    String repo = "hadoop-chef";
    String branch = "master";

    GithubUrls.Builder builder1 = new GithubUrls.Builder();
    GithubUrls urls1 = builder1.url("hopstart/hadoop-chef").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls1.home);
    assertEquals(rawHome, urls1.rawHome);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);

    GithubUrls.Builder builder2 = new GithubUrls.Builder();
    GithubUrls urls2 = builder2.url("hopstart/hadoop-chef/tree/master").build();
    assertEquals(id, urls2.id);
    assertEquals(home, urls2.home);
    assertEquals(rawHome, urls2.rawHome);
    assertEquals(metadataFile, urls2.metadataFile);
    assertEquals(attFile, urls2.attrFile);
    assertEquals(karamelFile, urls2.karamelFile);
    assertEquals(repo, urls2.repoName);
    assertEquals(branch, urls2.branch);

    GithubUrls.Builder builder3 = new GithubUrls.Builder();
    GithubUrls urls3 = builder3.url("https://github.com/hopstart/hadoop-chef").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls3.home);
    assertEquals(rawHome, urls3.rawHome);
    assertEquals(metadataFile, urls3.metadataFile);
    assertEquals(attFile, urls3.attrFile);
    assertEquals(karamelFile, urls3.karamelFile);
    assertEquals(repo, urls3.repoName);
    assertEquals(branch, urls3.branch);

    GithubUrls.Builder builder4 = new GithubUrls.Builder();
    GithubUrls urls4 = builder4.url("https://github.com/hopstart/hadoop-chef/tree/master").build();
    assertEquals(id, urls4.id);
    assertEquals(home, urls4.home);
    assertEquals(rawHome, urls4.rawHome);
    assertEquals(metadataFile, urls4.metadataFile);
    assertEquals(attFile, urls4.attrFile);
    assertEquals(karamelFile, urls4.karamelFile);
    assertEquals(repo, urls4.repoName);
    assertEquals(branch, urls4.branch);

    GithubUrls.Builder builder5 = new GithubUrls.Builder();
    GithubUrls urls5 = builder5.url("http://github.com/hopstart/hadoop-chef").build();
    assertEquals(id, urls5.id);
    assertEquals(home, urls5.home);
    assertEquals(rawHome, urls5.rawHome);
    assertEquals(metadataFile, urls5.metadataFile);
    assertEquals(attFile, urls5.attrFile);
    assertEquals(karamelFile, urls5.karamelFile);
    assertEquals(repo, urls5.repoName);
    assertEquals(branch, urls5.branch);

    GithubUrls.Builder builder6 = new GithubUrls.Builder();
    GithubUrls urls6 = builder6.url("http://github.com/hopstart/hadoop-chef/tree/master").build();
    assertEquals(id, urls6.id);
    assertEquals(home, urls6.home);
    assertEquals(rawHome, urls6.rawHome);
    assertEquals(metadataFile, urls6.metadataFile);
    assertEquals(attFile, urls6.attrFile);
    assertEquals(karamelFile, urls6.karamelFile);
    assertEquals(repo, urls6.repoName);
    assertEquals(branch, urls6.branch);
  }

  @Test
  public void testValidUrlsWithVersion() throws CookbookUrlException {
    String id = "https://github.com/hopstart/hadoop-chef/tree/v0.1";
    String home = "https://github.com/hopstart/hadoop-chef";
    String rawHome = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1";
    String metadataFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/metadata.rb";
    String attFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/attributes/default.rb";
    String karamelFile = "https://raw.githubusercontent.com/hopstart/hadoop-chef/v0.1/Karamelfile";
    String repo = "hadoop-chef";
    String branch = "v0.1";

    GithubUrls.Builder builder1 = new GithubUrls.Builder();
    GithubUrls urls1 = builder1.url("hopstart/hadoop-chef").branchOrVersion("v0.1").build();
    assertEquals(id, urls1.id);
    assertEquals(home, urls1.home);
    assertEquals(rawHome, urls1.rawHome);
    assertEquals(metadataFile, urls1.metadataFile);
    assertEquals(attFile, urls1.attrFile);
    assertEquals(karamelFile, urls1.karamelFile);
    assertEquals(repo, urls1.repoName);
    assertEquals(branch, urls1.branch);

    GithubUrls.Builder builder2 = new GithubUrls.Builder();
    GithubUrls urls2 = builder2.url("hopstart/hadoop-chef/tree/master").branchOrVersion("v0.1").build();
    assertEquals(id, urls2.id);
    assertEquals(home, urls2.home);
    assertEquals(rawHome, urls2.rawHome);
    assertEquals(metadataFile, urls2.metadataFile);
    assertEquals(attFile, urls2.attrFile);
    assertEquals(karamelFile, urls2.karamelFile);
    assertEquals(repo, urls2.repoName);
    assertEquals(branch, urls2.branch);

    GithubUrls.Builder builder3 = new GithubUrls.Builder();
    GithubUrls urls3 = builder3.url("https://github.com/hopstart/hadoop-chef").branchOrVersion("v0.1").build();
    assertEquals(id, urls3.id);
    assertEquals(home, urls3.home);
    assertEquals(rawHome, urls3.rawHome);
    assertEquals(metadataFile, urls3.metadataFile);
    assertEquals(attFile, urls3.attrFile);
    assertEquals(karamelFile, urls3.karamelFile);
    assertEquals(repo, urls3.repoName);
    assertEquals(branch, urls3.branch);

    GithubUrls.Builder builder4 = new GithubUrls.Builder();
    GithubUrls urls4 = builder4.url("https://github.com/hopstart/hadoop-chef/tree/master").branchOrVersion("v0.1").build();
    assertEquals(id, urls4.id);
    assertEquals(home, urls4.home);
    assertEquals(rawHome, urls4.rawHome);
    assertEquals(metadataFile, urls4.metadataFile);
    assertEquals(attFile, urls4.attrFile);
    assertEquals(karamelFile, urls4.karamelFile);
    assertEquals(repo, urls4.repoName);
    assertEquals(branch, urls4.branch);

    GithubUrls.Builder builder5 = new GithubUrls.Builder();
    GithubUrls urls5 = builder5.url("http://github.com/hopstart/hadoop-chef").branchOrVersion("v0.1").build();
    assertEquals(id, urls5.id);
    assertEquals(home, urls5.home);
    assertEquals(rawHome, urls5.rawHome);
    assertEquals(metadataFile, urls5.metadataFile);
    assertEquals(attFile, urls5.attrFile);
    assertEquals(karamelFile, urls5.karamelFile);
    assertEquals(repo, urls5.repoName);
    assertEquals(branch, urls5.branch);

    GithubUrls.Builder builder6 = new GithubUrls.Builder();
    GithubUrls urls6 = builder6.url("http://github.com/hopstart/hadoop-chef/tree/master").branchOrVersion("v0.1").build();
    assertEquals(id, urls6.id);
    assertEquals(home, urls6.home);
    assertEquals(rawHome, urls6.rawHome);
    assertEquals(metadataFile, urls6.metadataFile);
    assertEquals(attFile, urls6.attrFile);
    assertEquals(karamelFile, urls6.karamelFile);
  }
}
