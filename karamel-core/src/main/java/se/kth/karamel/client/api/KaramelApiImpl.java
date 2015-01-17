/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.HashSet;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import se.kth.karamel.backend.ClusterManager;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.launcher.amazon.Ec2Launcher;
import se.kth.karamel.backend.running.model.ClusterEntity;
import se.kth.karamel.backend.running.model.serializers.MakeSoloRbSerializer;
import se.kth.karamel.backend.running.model.serializers.RunRecipeTaskSerializer;
import se.kth.karamel.backend.running.model.serializers.ShellCommandSerializer;
import se.kth.karamel.backend.running.model.serializers.VendorCookbookSerializer;
import se.kth.karamel.backend.running.model.tasks.MakeSoloRbTask;
import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.backend.running.model.tasks.VendorCookbookTask;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.client.model.Cookbook;
import se.kth.karamel.client.model.Ec2;
import se.kth.karamel.cookbook.metadata.GithubCookbook;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.yaml.YamlCluster;
import se.kth.karamel.client.model.yaml.YamlGroup;
import se.kth.karamel.client.model.yaml.YamlPropertyRepresenter;
import se.kth.karamel.client.model.yaml.YamlUtil;

/**
 * Implementation of the Karamel Api for UI
 *
 * @author kamal
 */
public class KaramelApiImpl implements KaramelApi {

  private static final ClusterService clusterService = new ClusterService();
  
  @Override
  public String getCookbookDetails(String cookbookUrl, boolean refresh) throws KaramelException {
    if (refresh) {
      GithubCookbook cb = CookbookCache.load(cookbookUrl);
      return cb.getMetadataJson();
    } else {
      GithubCookbook cb = CookbookCache.get(cookbookUrl);
      return cb.getMetadataJson();
    }
  }

  @Override
  public String jsonToYaml(String json) throws KaramelException {
    Gson gson = new Gson();
    JsonCluster jsonCluster = gson.fromJson(json, JsonCluster.class);
    YamlCluster yamlCluster = new YamlCluster(jsonCluster);
    DumperOptions options = new DumperOptions();
    options.setIndent(2);
    options.setWidth(120);
    options.setExplicitEnd(false);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setPrettyFlow(true);
    options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    YamlPropertyRepresenter yamlPropertyRepresenter = new YamlPropertyRepresenter();
    yamlPropertyRepresenter.addClassTag(YamlCluster.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Ec2.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Cookbook.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(YamlGroup.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(HashSet.class, Tag.MAP);
    Yaml yaml = new Yaml(yamlPropertyRepresenter, options);
    String content = yaml.dump(yamlCluster);
    return content;
  }

  @Override
  public String yamlToJson(String yaml) throws KaramelException {
    try {
      YamlCluster cluster = YamlUtil.loadCluster(yaml);
      JsonCluster jsonCluster = new JsonCluster(cluster);
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(jsonCluster);
      return json;
    } catch (IOException ex) {
      throw new KaramelException("Could not convert yaml to java ", ex);
    }
  }

  @Override
  public boolean updateEc2CredentialsIfValid(String account, String accessKey) throws KaramelException {
    return Ec2Launcher.validateAndUpdateCredentials(account, accessKey);
  }

  @Override
  public String getClusterStatus(String clusterName) throws KaramelException {
    ClusterEntity clusterManager = clusterService.clusterStatus(clusterName);
    Gson gson = new GsonBuilder().
            registerTypeAdapter(ShellCommand.class, new ShellCommandSerializer()).
            registerTypeAdapter(RunRecipeTask.class, new RunRecipeTaskSerializer()).
            registerTypeAdapter(MakeSoloRbTask.class, new MakeSoloRbSerializer()).
            registerTypeAdapter(VendorCookbookTask.class, new VendorCookbookSerializer()).
            setPrettyPrinting().
            create();
    String json = gson.toJson(clusterManager);
    return json;
  }

  @Override
  public void pauseCluster(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void resumeCluster(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void purgeCluster(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void startCluster(String json) throws KaramelException {
    clusterService.startCluster(json);
  }

  @Override
  public String getInstallationDag(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
