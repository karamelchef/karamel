/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.exception.MetadataParseException;

/**
 * Represents a coobook located in github
 *
 * @author kamal
 */
public class GithubCookbook {

  private final GithubUrls urls;
  private final DefaultRb defaultRb;
  private final MetadataRb metadataRb;
  private final KaramelFile karamelFile;
  private final Berksfile berksfile;
  private String json;

  public GithubCookbook(String homeUrl) throws CookbookUrlException, MetadataParseException {
    GithubUrls.Builder builder = new GithubUrls.Builder();
    urls = builder.url(homeUrl).build();
    defaultRb = new DefaultRb(urls.attrFile);
    metadataRb = MetadataParser.parse(urls.home, urls.metadataFile);
    metadataRb.setDefaults(defaultRb);
    karamelFile = new KaramelFile(urls.karamelFile);
    berksfile = new Berksfile(urls.berksfile);
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
