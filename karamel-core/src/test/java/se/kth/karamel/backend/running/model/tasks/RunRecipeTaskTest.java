/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.stats.ClusterStats;

public class RunRecipeTaskTest {

  public RunRecipeTaskTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of merge method, of class RunRecipeTask.
   */
  @Test
  public void testMerge() {
    String dest = "{\n"
        + "  \"ndb\": {\n"
        + "    \"mgmd\": {\n"
        + "      \"public_key\": \"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDfhPNyp4MLJ3YsXAbupxYD7MH4LSDZn9u9wREKyCMZTLH6bJwlB7NvvIda0YiJyXBSfwVAPiTmAqtsSR7qXtIbxCjL98BiIpz/jliWkGHhg3vUv05WjPwNijNhHohPf56CRxseI/QdkPBkacDRGs0EbSYGHessIlZu21r/IF2vlN5uEkZ2AIGsgiQmmOerT5HHuxt6kKoX4Qxr9WDh5njY0nqOxh0uNNNaPBW54L6qLuVfRPj/sSeRCMzkWkeUBWVqeUXbDgWfS1sVu6rvv8Ajwl4wfYn2QtdTmNbkj8rTKey9AQvtZbsbZBhPZD/6zc5k8efscaHMRytZ2LUtuF0p root@ip-10-74-183-110\\n\"\n"
        + "    }\n"
        + "  }\n"
        + "}";

    String source = "{\n"
        + "  \"hdfs\": {\n"
        + "     \"user\" : \"jdowling\"\n"
        + "  },\n"
        + "  \"hops\": {\n"
        + "     \"nn\": {\n"
        + "        \"private_ips\": [\"127.0.0.1\"]\n"
        + "     },\n"
        + "     \"dn\": {\n"
        + "        \"private_ips\": [\"127.0.0.1\"]\n"
        + "     },\n"
        + "     \"rm\": {\n"
        + "        \"private_ips\": [\"127.0.0.1\"]\n"
        + "     },\n"
        + "     \"nm\": {\n"
        + "        \"private_ips\": [\"127.0.0.1\"]\n"
        + "     },\n"
        + "     \"jhs\": {\n"
        + "        \"private_ips\": [\"127.0.0.1\"]\n"
        + "     },\n"
        + "     \"yarn\": {\n"
        + "        \"user\" : \"jdowling\"\n"
        + "      },\n"
        + "     \"mr\": {\n"
        + "        \"user\" : \"jdowling\"\n"
        + "     },\n"
        + "     \"cluster\": \"vagrant\"\n"
        + "  },\n"
        + "  \"hadoop\": {\n"
        + "      \"yarn\":{\n"
        + "        \"user\" : \"jdowling\"\n"
        + "      },\n"
        + "      \"mr\":{\n"
        + "        \"user\": \"jdowling\"\n"
        + "      }\n"
        + "  },\n"
        + "  \"ndb\": {\n"
        + "     \"ndbd\": {\n"
        + "        \"private_ips\": [\"127.0.0.1\"]\n"
        + "     },\n"
        + "     \"mgmd\": {\n"
        + "        \"private_ips\": [\"127.0.0.1\"]\n"
        + "     },\n"
        + "     \"mysqld\": {\n"
        + "        \"private_ips\": [\"127.0.0.1\"]\n"
        + "     },\n"
        + "     \"memcached\": {\n"
        + "        \"private_ips\": [\"127.0.0.1\"]\n"
        + "     },\n"
        + "     \"public_ips\": [\"127.0.0.1\"],\n"
        + "     \"enabled\": \"true\",\n"
        + "     \"connectstring\": \"127.0.0.1:1186\"\n"
        + "  },\n"
        + "  \"kmon\": {\n"
        + "     \"private_ips\": [\"127.0.0.1\"],\n"
        + "     \"public_ips\": [\"127.0.0.1\"]\n"
        + "  },\n"
        + "  \"kagent\": {\n"
        + "     \"private_ips\": [\"127.0.0.1\"],\n"
        + "     \"public_ips\": [\"127.0.0.1\"]\n"
        + "  },\n"
        + "  \"private_ips\": [\"127.0.0.1\"],\n"
        + "  \"public_ips\": [\"127.0.0.1\"],\n"
        + "  \"run_list\": [ \n"
        + "    \"recipe[ndb::install]\",\n"
        + "    \"recipe[hops::install]\",\n"
        + "    \"recipe[ndb::mgmd]\",\n"
        + "    \"recipe[ndb::ndbd]\",\n"
        + "    \"recipe[ndb::mysqld]\",\n"
        + "    \"recipe[hops::ndb]\",\n"
        + "    \"recipe[hops::nn]\",\n"
        + "    \"recipe[hops::dn]\",\n"
        + "    \"recipe[hops::rm]\",\n"
        + "    \"recipe[hops::nm]\",\n"
        + "    \"recipe[hops::jhs]\"\n"
        + "  ]\n"
        + "}";

    JsonElement obj = new JsonParser().parse(source);
    JsonElement param = new JsonParser().parse(dest);
    MachineRuntime mr = new MachineRuntime(null);
    mr.setPublicIp("1111");
    ClusterStats clusterStats = new ClusterStats();
    RunRecipeTask instance = new RunRecipeTask("test dag", mr, clusterStats, "", "", null, "", "");
    JsonObject result = instance.merge(obj.getAsJsonObject(), param.getAsJsonObject());
    String modifiedJson = new Gson().toJson(result);
    System.out.println(modifiedJson);
    if (modifiedJson.compareToIgnoreCase(source) == 0) {
      fail("Merging json objects broken.");
    }
    if (result == null) {
      fail("The test case is a prototype.");
    }
  }

}
