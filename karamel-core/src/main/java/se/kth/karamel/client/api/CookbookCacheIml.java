package se.kth.karamel.client.api;


import org.apache.log4j.Logger;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.dag.DagNode;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.cookbookmeta.CookbookUrls;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import se.kth.karamel.common.util.ProcOutputConsumer;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class CookbookCacheIml implements CookbookCache {

  private static final Logger logger = Logger.getLogger(CookbookCacheIml.class);

  public Map<String, KaramelizedCookbook> cookbooks = new HashMap<>();
  public Set<String> problematics = new HashSet<>();

  private ExecutorService es = Executors.newFixedThreadPool(2);

  @Override
  public KaramelizedCookbook readNew(String cookbookUrl) throws KaramelException {
    problematics.remove(cookbookUrl);
    try {
      KaramelizedCookbook cookbook = new KaramelizedCookbook(cookbookUrl, false);
      cookbooks.put(cookbookUrl, cookbook);
      return cookbook;
    } catch (Exception e) {
      problematics.add(cookbookUrl);
      throw new NoKaramelizedCookbookException(
          String.format("Cookbook should problem in the metadata '%s'", cookbookUrl), e);
    }
  }

  @Override
  public KaramelizedCookbook get(String cookbookUrl) throws KaramelException {
    if (!problematics.contains(cookbookUrl)) {
      KaramelizedCookbook cb = cookbooks.get(cookbookUrl);
      if (cb == null) {
        cb = readNew(cookbookUrl);
      }
      return cb;
    } else {
      throw new NoKaramelizedCookbookException(String.format("Cookbook has problem in the metadata '%s'", cookbookUrl));
    }
  }

  @Override
  public synchronized void prepareParallel(Set<String> urlsToPrepare) throws KaramelException {
    Set<String> allUrls = new HashSet<>();
    for (String cookbookUrl : urlsToPrepare) {
      KaramelizedCookbook cb = cookbooks.get(cookbookUrl);
      if (cb == null) {
        CookbookUrls.Builder builder = new CookbookUrls.Builder();
        CookbookUrls urls = builder.url(cookbookUrl).build();
        allUrls.add(urls.metadataFile);
        allUrls.add(urls.karamelFile);
        allUrls.add(urls.berksFile);
      }
    }
    Map<String, String> contents = IoUtils.readContentParallel(allUrls, ClusterService.SHARED_GLOBAL_TP);
    for (String cookbookUrl : urlsToPrepare) {
      KaramelizedCookbook cb = cookbooks.get(cookbookUrl);
      if (cb == null) {
        CookbookUrls.Builder builder = new CookbookUrls.Builder();
        CookbookUrls urls = builder.url(cookbookUrl).build();
        if (contents.containsKey(urls.metadataFile)
            && contents.containsKey(urls.karamelFile)
            && contents.containsKey(urls.berksFile)) {
          KaramelizedCookbook kc = new KaramelizedCookbook(urls, contents.get(urls.metadataFile),
              contents.get(urls.karamelFile), contents.get(urls.berksFile));
          cookbooks.put(cookbookUrl, kc);
        } else {
          problematics.add(cookbookUrl);
        }
      }
    }
  }

  @Override
  public void prepareNewParallel(Set<String> cookbookUrls) throws KaramelException {
    Set<String> allUrls = new HashSet<>();
    for (String cookbookUrl : cookbookUrls) {
      CookbookUrls.Builder builder = new CookbookUrls.Builder();
      CookbookUrls urls = builder.url(cookbookUrl).build();
      allUrls.add(urls.metadataFile);
      allUrls.add(urls.karamelFile);
      allUrls.add(urls.berksFile);
    }
    Map<String, String> contents = IoUtils.readContentParallel(allUrls, ClusterService.SHARED_GLOBAL_TP);
    for (String cookbookUrl : cookbookUrls) {
      CookbookUrls.Builder builder = new CookbookUrls.Builder();
      CookbookUrls urls = builder.url(cookbookUrl).build();
      if (contents.containsKey(urls.metadataFile)
          && contents.containsKey(urls.karamelFile)
          && contents.containsKey(urls.berksFile)) {
        KaramelizedCookbook kc = new KaramelizedCookbook(urls, contents.get(urls.metadataFile),
            contents.get(urls.karamelFile), contents.get(urls.berksFile));
        cookbooks.put(cookbookUrl, kc);
      }
    }
  }

  @Override
  public List<KaramelizedCookbook> loadRootKaramelizedCookbooks(JsonCluster jsonCluster) throws KaramelException {
    List<JsonGroup> jsonGroups = jsonCluster.getGroups();
    Set<String> toLoad = new HashSet<>();
    for (JsonGroup jsonGroup : jsonGroups) {
      for (JsonCookbook cb : jsonGroup.getCookbooks()) {
        toLoad.add(cb.getId());
      }
    }
    for (JsonCookbook cb : jsonCluster.getCookbooks()) {
      toLoad.add(cb.getId());
    }
    Dag dag = new Dag();
    List<KaramelizedCookbook> kcbs = loadAllKaramelizedCookbooks(jsonCluster.getName(), toLoad, dag);
    List<KaramelizedCookbook> roots = new ArrayList<>();
    for (DagNode node : dag.findRootNodes()) {
      String id = node.getId();
      boolean found = false;
      for (KaramelizedCookbook kcb : kcbs) {
        if (kcb.getUrls().id.equals(id)) {
          roots.add(kcb);
          found = true;
          break;
        }
      }
      if (!found) {
        throw new NoKaramelizedCookbookException("Could not load a root cookbook, "
            + "make sure it is correctly karamelized " + id);
      }
    }
    return roots;
  }

  @Override
  public List<KaramelizedCookbook> loadAllKaramelizedCookbooks(YamlCluster cluster) throws KaramelException {
    Set<Cookbook> toClone = (HashSet<Cookbook>)cluster.getCookbooks().values();
    cloneAndVendorCookbooks(toClone);



    return null;
  }

  private void cloneAndVendorCookbooks(Set<Cookbook> toClone) throws KaramelException {
    for (Cookbook cb : toClone) {
      // Clone the repository
      try {
        Git.cloneRepository()
            // TODO(Fabio): make base url as setting in the cluster definition
            // So we can support also GitLab/Bitbucket and so on.
            .setURI(Settings.GITHUB_BASE_URL + "/" + cb.getGithub())
            .setBranch(cb.getBranch())
            .setDirectory(new File(Settings.WORKING_DIR))
            .call();
      } catch (GitAPIException e) {
        throw new KaramelException(e);
      }

      // Vendor the repository
      try {
        Process vendorProcess = Runtime.getRuntime().exec("berks vendor --berksfile=" +
            Paths.get(Settings.WORKING_DIR, cb.getCookbook(), "Berksfile") + " " + Settings.WORKING_DIR);
        Future<String> vendorOutput = es.submit(new ProcOutputConsumer(vendorProcess.getInputStream()));
        vendorProcess.waitFor(1, TimeUnit.MINUTES);

        if (vendorProcess.exitValue() != 0) {
          throw new KaramelException("Fail to vendor the cookbook: " + cb.getCookbook() + " " + vendorOutput.get());
        }
      } catch (IOException | InterruptedException | ExecutionException e) {
        throw new KaramelException(e);
      }
    }
  }
}
