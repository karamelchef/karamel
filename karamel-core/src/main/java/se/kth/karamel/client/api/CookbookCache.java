/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * It caches cookbooks' metadata that being read from Github
 */
package se.kth.karamel.client.api;

import java.util.HashMap;
import java.util.Map;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.cookbook.metadata.GithubCookbook;

/**
 *
 * @author kamal
 */
public class CookbookCache {
  
  public static Map<String, GithubCookbook> cookbooks = new HashMap<>();
  
  public static GithubCookbook load(String cookbookUrl) throws KaramelException {
    GithubCookbook cookbook = new GithubCookbook(cookbookUrl);
    cookbooks.put(cookbookUrl, cookbook);
    return cookbook;
  }
  
  public static GithubCookbook get(String cookbookUrl) throws KaramelException {
    GithubCookbook cb = cookbooks.get(cookbookUrl);
    if (cb == null)
      cb = load(cookbookUrl);
    return cb;
  }
}
