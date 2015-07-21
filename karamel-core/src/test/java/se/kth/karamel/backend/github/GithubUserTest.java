/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.github;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.Experiment;
import se.kth.karamel.backend.github.util.CookbookGenerator;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonCookbook;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.client.model.json.JsonRecipe;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.cookbook.metadata.KaramelFile;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlDependency;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlKaramelFile;

public class GithubUserTest {

  String user = "";
  String password = "";
  KaramelApi api;

  public GithubUserTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    api = new KaramelApiImpl();
    try {
      GithubUser u2 = api.loadGithubCredentials();
      user = u2.getUser();
      password = u2.getPassword();
    } catch (KaramelException ex) {
      fail(ex.getMessage());
    }
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getEmail method, of class GithubUser.
   */
  @Test
  public void testAccount() {
    try {
      api.registerGithubAccount(user, password);
      GithubUser u2 = api.loadGithubCredentials();
      assertEquals(this.user, u2.getUser());
      assertEquals(password, u2.getPassword());
      // TODO review the generated test code and remove the default call to fail.
    } catch (KaramelException ex) {
      Logger.getLogger(GithubUserTest.class.getName()).log(Level.SEVERE, null, ex);
      fail(ex.getMessage());
    }
  }

  /**
   * List Organizations in github
   */
  @Test
  public void testListOrgs() {
    try {
      api.registerGithubAccount(user, password);
      List<OrgItem> orgs = api.listGithubOrganizations();
      for (OrgItem o : orgs) {
        System.out.println("Organization: " + o.getName() + " : " + o.getGravitar());
      }
    } catch (KaramelException ex) {
      Logger.getLogger(GithubUserTest.class.getName()).log(Level.SEVERE, null, ex);
      fail(ex.getMessage());
    }
  }

  @Test
  public void testListRepos() {
    try {
      List<RepoItem> orgs = api.listGithubRepos("hopshadoop");
      for (RepoItem o : orgs) {
        System.out.println("Repo: " + o.getName() + " - " + o.getDescription() + " : " + o.getSshUrl());
      }
    } catch (KaramelException ex) {
      Logger.getLogger(GithubUserTest.class.getName()).log(Level.SEVERE, null, ex);
      fail(ex.getMessage());
    }
  }

  @Test
  public void testKaramelfile() {

    try {
      StringBuilder karamelContents = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_KARAMELFILE,
          "name", "jim"
      );
      KaramelFile karamelFile = new KaramelFile(karamelContents.toString());

      String ymlString = "name: MySqlCluster\n"
          + "ec2:\n"
          + "    type: m3.medium\n"
          + "    region: eu-west-1\n"
          + "\n"
          + "cookbooks:\n"
          + "  ndb:\n"
          + "    github: \"hopshadoop/ndb-chef\"\n"
          + "    branch: \"master\"\n"
          + "    \n"
          + "groups: \n"
          + "  nodes:\n"
          + "    size: 1 \n"
          + "    recipes: \n"
          + "        - ndb::mgmd\n"
          + "        - ndb::ndbd\n"
          + "        - ndb::mysqld\n"
          + "        - ndb::memcached";
      JsonCluster jsonCluster = ClusterDefinitionService.yamlToJsonObject(ymlString);
      YamlDependency yd = new YamlDependency();
      List<String> clusterDependencies = new ArrayList<>();
      for (JsonGroup g : jsonCluster.getGroups()) {
        for (JsonCookbook cb : g.getCookbooks()) {
          for (JsonRecipe r : cb.getRecipes()) {
            clusterDependencies.add(r.getCanonicalName());
          }
        }
      }
      yd.setRecipe("test::test");
      yd.setGlobal(clusterDependencies);
      yd.setLocal(null);
      List<YamlDependency> yds = karamelFile.getDependencies();
      yds.add(yd);
      DumperOptions options = new DumperOptions();
      options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
      Representer r = new Representer();
      r.addClassTag(KaramelFile.class, Tag.MAP);
      Yaml karamelYml = new Yaml(new Constructor(YamlKaramelFile.class), r, options);
      String karamelFileContents = karamelYml.dump(karamelFile);

      File f = File.createTempFile("karamelfile", "out");
      try (PrintWriter out = new PrintWriter(f)) {
        out.println(karamelFileContents);
      }
      String contents = Files.toString(f, Charsets.UTF_8);
      KaramelFile karamelFile2 = new KaramelFile(contents);
    } catch (KaramelException | IOException ex) {
      fail(ex.getMessage());
    }

  }

  @Test
  public void testCreateRepo() {
    try {
      Experiment ec = new Experiment();
      Experiment.Code exp = new Experiment.Code("experiment", "echo \"jim\"\n"
          + "java -jar -D%%maxHeapSize%% prog.jar", "config.props", "%%maxHeapSize%%=128m\n%%log%%=true\n",
          "bash");
      List<Experiment.Code> exps = ec.getCode();
      exps.add(exp);      
      ec.setUser("blah");
      ec.setGroup("blah");
//      ec.setResultsDir("results");
      ec.setUrlBinary("http://snurran.sics.se/hops/prog.jar");
      ec.setDescription("Test experiment");
      ec.setClusterDefinition("name: MySqlCluster\n"
          + "ec2:\n"
          + "    type: m3.medium\n"
          + "    region: eu-west-1\n"
          + "\n"
          + "cookbooks:\n"
          + "  ndb:\n"
          + "    github: \"hopshadoop/ndb-chef\"\n"
          + "    branch: \"master\"\n"
          + "    \n"
          + "groups: \n"
          + "  nodes:\n"
          + "    size: 1 \n"
          + "    recipes: \n"
          + "        - ndb::mgmd\n"
          + "        - ndb::ndbd\n"
          + "        - ndb::mysqld\n"
          + "        - ndb::memcached"
      );
      
      api.commitAndPushExperiment(ec);

    } catch (KaramelException ex) {
      fail(ex.getMessage());
    }
  }

}
