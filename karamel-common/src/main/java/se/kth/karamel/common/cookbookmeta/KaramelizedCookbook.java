package se.kth.karamel.common.cookbookmeta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class KaramelizedCookbook {

  private String cookbookName;
  private MetadataRb metadataRb;
  private KaramelFile karamelFile;
  private String json;

  public KaramelizedCookbook(MetadataRb metadata, KaramelFile karamelFile) {
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

  public String getCookbookName() {
    return cookbookName;
  }

  public void setCookbookName(String cookbookName) {
    this.cookbookName = cookbookName;
  }

  public void setMetadataRb(MetadataRb metadataRb) {
    this.metadataRb = metadataRb;
  }

  public void setKaramelFile(KaramelFile karamelFile) {
    this.karamelFile = karamelFile;
  }
}
