/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.RecipeParseException;
import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.util.IoUtils;
import se.kth.karamel.common.util.Settings;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;

/**
 *
 * @author kamal
 */
public class KaramelizedCookbookTest {

  @Test
  public void testGetInfoJson() throws CookbookUrlException, MetadataParseException, ValidationException, NoKaramelizedCookbookException {
    Settings.CB_CLASSPATH_MODE = true;
    KaramelizedCookbook cb = new KaramelizedCookbook("testorg/testrepo/tree/master/cookbooks/biobankcloud/hiway-chef", false);
    String json = cb.getInfoJson();
    String expecetdJson = "{\n"
        + "  \"id\": \"https://github.com/testorg/testrepo/tree/master/cookbooks/biobankcloud/hiway-chef\",\n"
        + "  \"name\": \"hiway\",\n"
        + "  \"description\": \"Chef recipes for installing Hi-WAY, its dependencies, and several workflows.\",\n"
        + "  \"version\": \"1.0.0\",\n"
        + "  \"attributes\": [\n"
        + "    {\n"
        + "      \"name\": \"hiway/user\",\n"
        + "      \"displayName\": \"Name of the Hi-WAY user\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"description\": \"Name of the Hi-WAY user\",\n"
        + "      \"default\": \"hiway\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/data\",\n"
        + "      \"displayName\": \"Data directory\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"description\": \"Directory in which to store large data, e.g., input data of the workflow\",\n"
        + "      \"default\": \"/home/hiway\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/release\",\n"
        + "      \"displayName\": \"Release or snaphsot\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"description\": \"Install Hi-WAY release as opposed to the latest snapshot version\",\n"
        + "      \"default\": \"false\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/hiway/am/memory_mb\",\n"
        + "      \"displayName\": \"Hi-WAY Application Master Memory in MB\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"description\": \"Amount of memory in MB to be requested to run the application master.\",\n"
        + "      \"default\": \"512\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/hiway/am/vcores\",\n"
        + "      \"displayName\": \"Hi-WAY Application Master Number of Virtual Cores\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"description\": \"Hi-WAY Application Master Number of Virtual Cores\",\n"
        + "      \"default\": \"1\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/hiway/worker/memory_mb\",\n"
        + "      \"displayName\": \"Hi-WAY Worker Memory in MB\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"description\": \"Hi-WAY Worker Memory in MB\",\n"
        + "      \"default\": \"1024\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/hiway/worker/vcores\",\n"
        + "      \"displayName\": \"Hi-WAY Worker Number of Virtual Cores\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"description\": \"Hi-WAY Worker Number of Virtual Cores\",\n"
        + "      \"default\": \"1\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/hiway/scheduler\",\n"
        + "      \"displayName\": \"Hi-WAY Scheduler\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"description\": \"valid values: c3po, cloning, conservative, greedyQueue, heft, outlooking, placementAware, staticRoundRobin\",\n"
        + "      \"default\": \"placementAware\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/variantcall/reads/sample_id\",\n"
        + "      \"displayName\": \"1000 Genomes Sample Id\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"description\": \"The Sample Id of sequence data from the 1000 Genomes project that is to be aligned\",\n"
        + "      \"default\": \"HG02025\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/variantcall/reads/run_ids\",\n"
        + "      \"displayName\": \"1000 Genomes Run Ids\",\n"
        + "      \"type\": \"array\",\n"
        + "      \"description\": \"The Run Ids of sequence data from the 1000 Genomes project that is to be aligned\",\n"
        + "      \"default\": [\n"
        + "        \"SRR359188\",\n"
        + "        \"SRR359195\"\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway/variantcall/reference/chromosomes\",\n"
        + "      \"displayName\": \"HG38 chromosomes\",\n"
        + "      \"type\": \"array\",\n"
        + "      \"description\": \"The chromosomes of the HG38 reference against which sequence data is to be aligned\",\n"
        + "      \"default\": [\n"
        + "        \"chr22\",\n"
        + "        \"chrY\"\n"
        + "      ]\n"
        + "    }\n"
        + "  ],\n"
        + "  \"recipes\": [\n"
        + "    {\n"
        + "      \"name\": \"hiway::install\",\n"
        + "      \"description\": \"Installs and sets up Hi-WAY\",\n"
        + "      \"links\": []\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"hiway::hiway_client\",\n"
        + "      \"description\": \"Configures Hadoop to support Hi-WAY on the Client\",\n"
        + "      \"links\": []\n"
        + "    }\n"
        + "  ]\n"
        + "}";

    Assert.assertEquals(expecetdJson, json);
  }

  @Test
  public void testLoadingClasspathCookbook() throws ValidationException, NoKaramelizedCookbookException {
    try {
      Settings.CB_CLASSPATH_MODE = true;
      KaramelizedCookbook cb = new KaramelizedCookbook("testorg/testrepo/tree/master/cookbooks/hopshadoop/hopsworks-chef", false);
    } catch (CookbookUrlException | MetadataParseException e) {
      Assert.fail();
    }
  }

  @Test
  public void testMetadata() throws MetadataParseException, IOException {
    String file = Resources.toString(Resources.getResource("se/kth/karamel/cookbook/metadata/metadata.rb"),
        Charsets.UTF_8);
    MetadataRb metadatarb = MetadataParser.parse(file);
    List<Recipe> recipes = metadatarb.getRecipes();
    assertEquals(recipes.size(), 2);
    Recipe r1 = recipes.get(0);
    Recipe r2 = recipes.get(1);
    assertEquals(r1.getName(), "hopsworks::install");
    Set<String> l1 = r1.getLinks();
    assertEquals(l1.size(), 0);
    assertEquals(r2.getName(), "hopsworks::default");
    Set<String> l2 = r2.getLinks();
    assertEquals(l2.size(), 2);
//    assertEquals(l2.toArray()[0], "Click {here,https://%host%:8181/hop-dashboard} to launch hopsworks in your browser");
//    assertEquals(l2.toArray()[1], "Visit Karamel {here,www.karamel.io}");
  }

  @Test
  public void testLoadDependencies() throws CookbookUrlException, IOException {
    Settings.CB_CLASSPATH_MODE = true;
    String content = IoUtils.readContentFromClasspath("testgithub/testorg/testrepo/master/cookbooks/hopshadoop/hopsworks-chef/Berksfile");
    Berksfile berksfile = new Berksfile(content);
  }

  @Test
  public void testParseRecipes() throws CookbookUrlException, IOException {
    try {
      Settings.CB_CLASSPATH_MODE = true;
      String recipe = Resources.toString(Resources.getResource(
          "testgithub/testorg/testrepo/master/cookbooks/hopshadoop/hopsworks-chef/recipes/experiment.rb"), Charsets.UTF_8);
      ExperimentRecipe er = ExperimentRecipeParser.parse("experiment", recipe, "config.props", "x=y");
      assertEquals("experiment", er.getRecipeName());
      assertEquals(er.getConfigFileName().isEmpty(), false);
      assertEquals(er.getConfigFileContents().isEmpty(), false);
      assertEquals(er.getScriptContents().isEmpty(), false);
      assertEquals(er.getScriptType(), "bash");
    } catch (RecipeParseException ex) {
      Assert.fail(ex.toString());
    }

  }
}
