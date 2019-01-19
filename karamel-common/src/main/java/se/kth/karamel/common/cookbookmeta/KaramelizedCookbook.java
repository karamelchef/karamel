package se.kth.karamel.common.cookbookmeta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;
import se.kth.karamel.common.exception.ValidationException;

public class KaramelizedCookbook {

  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(KaramelizedCookbook.class);

  private final Cookbook cookbook;
  private final MetadataRb metadataRb;
  private final KaramelFile karamelFile;
  private String json;

  /**
   *
   * @param homeUrl url or canonical path to the cookbook
   * @param local true if it is a canonical path (to a cloned cookbook) and not a URL.
   * @throws CookbookUrlException
   * @throws MetadataParseException
   * @throws se.kth.karamel.common.exception.ValidationException
   * @throws se.kth.karamel.common.exception.NoKaramelizedCookbookException
   */
  public KaramelizedCookbook(String homeUrl, boolean local) throws CookbookUrlException, MetadataParseException,
      ValidationException, NoKaramelizedCookbookException {
    if (local) {
      Settings.USE_CLONED_REPO_FILES = true;
    }
    CookbookUrls.Builder builder = new CookbookUrls.Builder();
    this.urls = builder.url(homeUrl).build();
    String karamelFileC;
    try {
      karamelFileC = IoUtils.readContent(urls.karamelFile);
    } catch (IOException e) {
      throw new NoKaramelizedCookbookException(e);
    }
    try {
      String metadataRbC = IoUtils.readContent(urls.metadataFile);
      String berksfileC = IoUtils.readContent(urls.berksFile);
      this.metadataRb = MetadataParser.parse(metadataRbC);
      this.metadataRb.normalizeRecipeNames();
      this.karamelFile = new KaramelFile(karamelFileC);
      this.berksFile = new Berksfile(berksfileC);
    } catch (IOException e) {
      Settings.USE_CLONED_REPO_FILES = false;
      throw new CookbookUrlException("", e);
    }
  }

  public KaramelizedCookbook(CookbookUrls urls, String metadataRbC, String karamelFileC,
      String berksfileC) throws CookbookUrlException, MetadataParseException, ValidationException {
    this.urls = urls;
    this.metadataRb = MetadataParser.parse(metadataRbC);
    this.metadataRb.normalizeRecipeNames();
    this.karamelFile = new KaramelFile(karamelFileC);
    this.berksFile = new Berksfile(berksfileC);
  }

  public String getInfoJson() {
    if (json == null) {
      CookbookInfoJson cookbookInfoJson = new CookbookInfoJson(urls.id, metadataRb);
      GsonBuilder builder = new GsonBuilder();
      builder.disableHtmlEscaping();
      Gson gson = builder.setPrettyPrinting().create();
      json = gson.toJson(cookbookInfoJson);
    }
    return json;
  }

  public MetadataRb getMetadataRb() {
    return metadataRb;
  }

  public KaramelFile getKaramelFile() {
    return karamelFile;
  }

  public CookbookUrls getUrls() {
    return urls;
  }

  public void addDependency(KaramelizedCookbook cookbook) {
    this.dependencies.add(cookbook);
  }

  public void addDependencies(Set<KaramelizedCookbook> cookbooks) {
    this.dependencies.addAll(cookbooks);
  }

  public Set<KaramelizedCookbook> getDependencies() { return this.dependencies; }
}
