package se.kth.karamel.client.api;


import com.google.common.base.Charsets;
import org.apache.log4j.Logger;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.cookbookmeta.KaramelFile;
import se.kth.karamel.common.cookbookmeta.MetadataParser;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.stream.Stream;

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
  public KaramelizedCookbook get(String cookbookName) throws KaramelException {
    if (!problematics.contains(cookbookName)) {
      KaramelizedCookbook cb = cookbooks.get(cookbookName);
      if (cb == null) {
        throw new NoKaramelizedCookbookException(
            String.format("Cookbook could not be found '%s'", cookbookName));
      }
      return cb;
    } else {
      throw new NoKaramelizedCookbookException(
          String.format("Cookbook has problem in the metadata '%s'", cookbookName));
    }
  }

  @Override
  public List<KaramelizedCookbook> loadAllKaramelizedCookbooks(JsonCluster jsonCluster) throws KaramelException {
    if (!cookbooks.isEmpty()) {
      return new ArrayList<>(cookbooks.values());
    }

    Set<Cookbook> toClone = (HashSet<Cookbook>)jsonCluster.getRootCookbooks().values();
    cloneAndVendorCookbooks(toClone);
    buildCookbookObjects();
    return new ArrayList<>(cookbooks.values());
  }

  @Override
  public List<KaramelizedCookbook> loadAllKaramelizedCookbooks(YamlCluster cluster) throws KaramelException {
    if (!cookbooks.isEmpty()) {
      return new ArrayList<>(cookbooks.values());
    }

    Set<Cookbook> toClone = (HashSet<Cookbook>)cluster.getCookbooks().values();
    cloneAndVendorCookbooks(toClone);
    buildCookbookObjects();
    return new ArrayList<>(cookbooks.values());
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

  private void buildCookbookObjects() throws KaramelException {
    try (Stream<Path> paths = Files.find(Paths.get(Settings.WORKING_DIR),
        Integer.MAX_VALUE, (path, attributes) -> attributes.isDirectory())) {
      paths.forEach(path -> {
          File rawKaramelFile = path.resolve("Karamelfile").toFile();
          File rawMetadataRb = path.resolve("metadata.rb").toFile();
          if (rawKaramelFile.exists()) {
            try {
              KaramelFile karamelFile = new KaramelFile(com.google.common.io.Files.toString(
                  rawKaramelFile, Charsets.UTF_8));
              MetadataRb metadataRb = MetadataParser.parse(com.google.common.io.Files.toString(
                  rawMetadataRb, Charsets.UTF_8));
              cookbooks.put(metadataRb.getName(), new KaramelizedCookbook(metadataRb, karamelFile));
            } catch (IOException | MetadataParseException e) {
              logger.error(e);
              throw new RuntimeException(e);
            }
          }
        });
    } catch (IOException e) {
      throw new KaramelException(e);
    }
  }
}
