package se.kth.karamel.common.cookbookmeta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import se.kth.karamel.common.clusterdef.Cookbook;

public class KaramelizedCookbook {

  private final String cookbookName;
  private final Cookbook cookbook;
  private final MetadataRb metadataRb;
  private final KaramelFile karamelFile;
  private String json;

  public KaramelizedCookbook(Cookbook cookbook, MetadataRb metadata, KaramelFile karamelFile) {
    this.cookbook = cookbook;
    this.cookbookName = metadata.getName();
    this.metadataRb = metadata;
    this.karamelFile = karamelFile;
  }

  // TODO(Fabio): this is probably useless here.
  public String getInfoJson() {
    if (json == null) {
      CookbookInfoJson cookbookInfoJson = new CookbookInfoJson(metadataRb);
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

  public Cookbook getCookbook() {
    return cookbook;
  }

  public String getCookbookName() {
    return cookbookName;
  }
}
