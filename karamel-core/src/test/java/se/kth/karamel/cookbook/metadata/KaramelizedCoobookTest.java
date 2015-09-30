/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.cookbook.metadata;

import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class KaramelizedCoobookTest {
  
  @Test
  public void testGetInfoJson() throws CookbookUrlException, MetadataParseException, ValidationException {
    Settings.CB_CLASSPATH_MODE = true;
    KaramelizedCookbook cb = new KaramelizedCookbook("testorg/testrepo/tree/master/cookbooks/biobankcloud/hiway-chef", false);
    String json = cb.getInfoJson();
    String expecetdJson = "{\n" +
"  \"id\": \"https://github.com/testorg/testrepo/tree/master/cookbooks/biobankcloud/hiway-chef\",\n" +
"  \"name\": \"hiway\",\n" +
"  \"description\": \"Chef recipes for installing Hi-WAY, its dependencies, and several workflows.\",\n" +
"  \"version\": \"1.0.0\",\n" +
"  \"attributes\": [\n" +
"    {\n" +
"      \"name\": \"hiway/user\",\n" +
"      \"displayName\": \"Name of the Hi-WAY user\",\n" +
"      \"type\": \"string\",\n" +
"      \"description\": \"Name of the Hi-WAY user\",\n" +
"      \"default\": \"hiway\"\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/data\",\n" +
"      \"displayName\": \"Data directory\",\n" +
"      \"type\": \"string\",\n" +
"      \"description\": \"Directory in which to store large data, e.g., input data of the workflow\",\n" +
"      \"default\": \"/home/hiway\"\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/release\",\n" +
"      \"displayName\": \"Release or snaphsot\",\n" +
"      \"type\": \"string\",\n" +
"      \"description\": \"Install Hi-WAY release as opposed to the latest snapshot version\",\n" +
"      \"default\": \"false\"\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/hiway/am/memory_mb\",\n" +
"      \"displayName\": \"Hi-WAY Application Master Memory in MB\",\n" +
"      \"type\": \"string\",\n" +
"      \"description\": \"Amount of memory in MB to be requested to run the application master.\",\n" +
"      \"default\": \"512\"\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/hiway/am/vcores\",\n" +
"      \"displayName\": \"Hi-WAY Application Master Number of Virtual Cores\",\n" +
"      \"type\": \"string\",\n" +
"      \"description\": \"Hi-WAY Application Master Number of Virtual Cores\",\n" +
"      \"default\": \"1\"\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/hiway/worker/memory_mb\",\n" +
"      \"displayName\": \"Hi-WAY Worker Memory in MB\",\n" +
"      \"type\": \"string\",\n" +
"      \"description\": \"Hi-WAY Worker Memory in MB\",\n" +
"      \"default\": \"1024\"\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/hiway/worker/vcores\",\n" +
"      \"displayName\": \"Hi-WAY Worker Number of Virtual Cores\",\n" +
"      \"type\": \"string\",\n" +
"      \"description\": \"Hi-WAY Worker Number of Virtual Cores\",\n" +
"      \"default\": \"1\"\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/hiway/scheduler\",\n" +
"      \"displayName\": \"Hi-WAY Scheduler\",\n" +
"      \"type\": \"string\",\n" +
"      \"description\": \"valid values: c3po, cloning, conservative, greedyQueue, heft, outlooking, placementAware, staticRoundRobin\",\n" +
"      \"default\": \"placementAware\"\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/variantcall/reads/sample_id\",\n" +
"      \"displayName\": \"1000 Genomes Sample Id\",\n" +
"      \"type\": \"string\",\n" +
"      \"description\": \"The Sample Id of sequence data from the 1000 Genomes project that is to be aligned\",\n" +
"      \"default\": \"HG02025\"\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/variantcall/reads/run_ids\",\n" +
"      \"displayName\": \"1000 Genomes Run Ids\",\n" +
"      \"type\": \"array\",\n" +
"      \"description\": \"The Run Ids of sequence data from the 1000 Genomes project that is to be aligned\",\n" +
"      \"default\": [\n" +
"        \"SRR359188\",\n" +
"        \"SRR359195\"\n" +
"      ]\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway/variantcall/reference/chromosomes\",\n" +
"      \"displayName\": \"HG38 chromosomes\",\n" +
"      \"type\": \"array\",\n" +
"      \"description\": \"The chromosomes of the HG38 reference against which sequence data is to be aligned\",\n" +
"      \"default\": [\n" +
"        \"chr22\",\n" +
"        \"chrY\"\n" +
"      ]\n" +
"    }\n" +
"  ],\n" +
"  \"recipes\": [\n" +
"    {\n" +
"      \"name\": \"hiway::install\",\n" +
"      \"description\": \"Installs and sets up Hi-WAY\",\n" +
"      \"links\": []\n" +
"    },\n" +
"    {\n" +
"      \"name\": \"hiway::hiway_client\",\n" +
"      \"description\": \"Configures Hadoop to support Hi-WAY on the Client\",\n" +
"      \"links\": []\n" +
"    }\n" +
"  ]\n" +
"}";
    
    Assert.assertEquals(expecetdJson, json);
  }
}
