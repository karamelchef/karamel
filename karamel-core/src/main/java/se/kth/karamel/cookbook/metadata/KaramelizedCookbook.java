/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.List;
import se.kth.karamel.common.IoUtils;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.exception.MetadataParseException;

/**
 * Represents a coobook located in github
 *
 * @author kamal
 */
public class KaramelizedCookbook {

  private final CookbookUrls urls;
  private final DefaultRb defaultRb;
  private final MetadataRb metadataRb;
  private final KaramelFile karamelFile;
  private final Berksfile berksfile;
  private String json;

  public KaramelizedCookbook(String homeUrl) throws CookbookUrlException, MetadataParseException {
    try {
      CookbookUrls.Builder builder = new CookbookUrls.Builder();
      urls = builder.url(homeUrl).build();
      List<String> lines1 = IoUtils.readLines(urls.attrFile);
      defaultRb = new DefaultRb(lines1);
      String metadataContent = IoUtils.readContent(urls.metadataFile);
      metadataRb = MetadataParser.parse(metadataContent);
      metadataRb.setDefaults(defaultRb);
      String karamelFileConent = IoUtils.readContent(urls.karamelFile);
      karamelFile = new KaramelFile(karamelFileConent);
      List<String> berksfileLines = IoUtils.readLines(urls.berksfile);
      berksfile = new Berksfile(berksfileLines);
    } catch (IOException e) {
      throw new CookbookUrlException("", e);
    }
  }

  public String getMetadataJson() {
    if (json == null) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      json = gson.toJson(metadataRb);
    }
    return json;
  }

  public MetadataRb getMetadataRb() {
    return metadataRb;
  }

  public KaramelFile getKaramelFile() {
    return karamelFile;
  }

}
