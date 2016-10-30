/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class BerksfileTest {

  @Test
  public void testLinePatterns() throws CookbookUrlException {
    Settings.CB_CLASSPATH_MODE = true;
    String content = "cookbook 'kagent', github: 'karamelchef/kagent-chef'\n"
        + "cookbook 'ark', github: 'burtlo/ark', tag: 'v0.8.2'\n"
        + "cookbook 'ark2', github: 'burtlo/ark2', branch: 'kitchen'\n"
        + "cookbook 'ark3', github: 'burtlo/ark3', version: 'v0.4.0'\n";
    Berksfile berksfile = new Berksfile(content);
    Assert.assertTrue(berksfile.getDeps().containsKey("kagent"));
    Assert.assertEquals("karamelchef/kagent-chef", berksfile.getDeps().get("kagent").getGithub());
    Assert.assertEquals("master", berksfile.getDeps().get("kagent").getBranch());
    Assert.assertTrue(berksfile.getDeps().containsKey("ark"));
    Assert.assertEquals("burtlo/ark", berksfile.getDeps().get("ark").getGithub());
    Assert.assertEquals("v0.8.2", berksfile.getDeps().get("ark").getBranch());
    Assert.assertTrue(berksfile.getDeps().containsKey("ark2"));
    Assert.assertEquals("burtlo/ark2", berksfile.getDeps().get("ark2").getGithub());
    Assert.assertEquals("kitchen", berksfile.getDeps().get("ark2").getBranch());
    Assert.assertTrue(berksfile.getDeps().containsKey("ark3"));
    Assert.assertEquals("burtlo/ark3", berksfile.getDeps().get("ark3").getGithub());
    Assert.assertEquals("v0.4.0", berksfile.getDeps().get("ark3").getBranch());
  }

  @Test
  public void testHopsworksBerksfile() throws CookbookUrlException {
    String content = "Encoding.default_external = \"UTF-8\"\n"
        + "source 'https://supermarket.chef.io'\n"
        + "\n"
        + "\n"
        + "cookbook 'java'\n"
        + "cookbook 'kagent', github: \"karamelchef/kagent-chef\", branch: \"master\"\n"
        + "cookbook 'apache_hadoop', github: \"hopshadoop/apache-hadoop-chef\", branch: \"master\"\n"
        + "cookbook 'hops', github: \"hopshadoop/hops-hadoop-chef\", branch: \"master\"\n"
        + "cookbook 'ndb', github: \"hopshadoop/ndb-chef\", branch: \"master\"\n"
        + "cookbook 'hadoop_spark', github: \"hopshadoop/spark-chef\", branch: \"master\"\n"
        + "cookbook 'flink', github: \"hopshadoop/flink-chef\", branch: \"master\"\n"
        + "cookbook 'zeppelin', github: \"hopshadoop/zeppelin-chef\", branch: \"master\"\n"
        + "cookbook 'livy', github: \"hopshadoop/livy-chef\", branch: \"master\"\n"
        + "cookbook 'drelephant', github: \"hopshadoop/dr-elephant-chef\", branch: \"master\"\n"
        + "cookbook 'tensorflow', github: \"hopshadoop/tensorflow-chef\", branch: \"master\"\n"
        + "\n"
        + "cookbook 'epipe', github: \"hopshadoop/epipe-chef\", branch: \"master\"\n"
        + "cookbook 'adam', github: \"biobankcloud/adam-chef\", branch: \"master\"\n"
        + "cookbook 'dela', github: \"hopshadoop/dela-chef\", branch: \"master\"\n"
        + "\n"
        + "cookbook 'kzookeeper', github: \"hopshadoop/kzookeeper\", branch: \"master\"\n"
        + "cookbook 'kkafka', github: \"hopshadoop/kafka-cookbook\", branch: \"master\"\n"
        + "cookbook 'elastic', github: \"hopshadoop/elasticsearch-chef\", branch: \"master\"\n"
        + "cookbook 'kibana', github: \"hopshadoop/kibana-chef\", branch: \"master\"\n"
        + "\n"
        + "cookbook 'hopsmonitor', github: \"hopshadoop/hopsmonitor-chef\", branch: \"master\"\n"
        + "#cookbook 'chef-grafana', github: \"chef-cookbooks/chef-grafana\", branch: \"master\"\n"
        + "cookbook 'graphite', '~> 1.0.4'\n"
        + "cookbook 'simple-logstash', '~> 0.2.4'\n"
        + "\n"
        + "cookbook 'glassfish', github: \"realityforge/chef-glassfish\", branch: \"master\"\n"
        + "\n"
        + "cookbook 'compat_resource', '~> 12.7.3'\n"
        + "cookbook 'ulimit2', '~> 0.2.0'\n"
        + "cookbook 'authbind', '~> 0.1.10'\n"
        + "cookbook 'ntp', '~> 2.0.0'\n"
        + "\n"
        + "#cookbook 'collectd', github: \"hopshadoop/collectd-chef\", branch: \"master\"\n"
        + "\n"
        + "metadata";

    Berksfile berksfile = new Berksfile(content);
    Assert.assertFalse(berksfile.getDeps().containsKey("java"));

    Assert.assertTrue(berksfile.getDeps().containsKey("kagent"));
    Assert.assertEquals("master", berksfile.getDeps().get("kagent").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("apache_hadoop"));
    Assert.assertEquals("master", berksfile.getDeps().get("apache_hadoop").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("hops"));
    Assert.assertEquals("master", berksfile.getDeps().get("hops").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("ndb"));
    Assert.assertEquals("master", berksfile.getDeps().get("ndb").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("hadoop_spark"));
    Assert.assertEquals("master", berksfile.getDeps().get("hadoop_spark").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("flink"));
    Assert.assertEquals("master", berksfile.getDeps().get("flink").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("zeppelin"));
    Assert.assertEquals("master", berksfile.getDeps().get("zeppelin").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("livy"));
    Assert.assertEquals("master", berksfile.getDeps().get("livy").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("drelephant"));
    Assert.assertEquals("master", berksfile.getDeps().get("drelephant").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("tensorflow"));
    Assert.assertEquals("master", berksfile.getDeps().get("tensorflow").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("epipe"));
    Assert.assertEquals("master", berksfile.getDeps().get("epipe").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("adam"));
    Assert.assertEquals("master", berksfile.getDeps().get("adam").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("dela"));
    Assert.assertEquals("master", berksfile.getDeps().get("dela").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("kzookeeper"));
    Assert.assertEquals("master", berksfile.getDeps().get("kzookeeper").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("kkafka"));
    Assert.assertEquals("master", berksfile.getDeps().get("kkafka").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("elastic"));
    Assert.assertEquals("master", berksfile.getDeps().get("elastic").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("kibana"));
    Assert.assertEquals("master", berksfile.getDeps().get("kibana").getBranch());

    Assert.assertTrue(berksfile.getDeps().containsKey("hopsmonitor"));
    Assert.assertEquals("master", berksfile.getDeps().get("hopsmonitor").getBranch());

    Assert.assertFalse(berksfile.getDeps().containsKey("chef-grafana"));

    Assert.assertFalse(berksfile.getDeps().containsKey("graphite"));

    Assert.assertFalse(berksfile.getDeps().containsKey("simple-logstash"));

    Assert.assertTrue(berksfile.getDeps().containsKey("glassfish"));
    Assert.assertEquals("master", berksfile.getDeps().get("glassfish").getBranch());

    Assert.assertFalse(berksfile.getDeps().containsKey("compat_resource"));

    Assert.assertFalse(berksfile.getDeps().containsKey("ulimit2"));

    Assert.assertFalse(berksfile.getDeps().containsKey("authbind"));

    Assert.assertFalse(berksfile.getDeps().containsKey("authbind"));

    Assert.assertFalse(berksfile.getDeps().containsKey("collectd"));

  }
}
