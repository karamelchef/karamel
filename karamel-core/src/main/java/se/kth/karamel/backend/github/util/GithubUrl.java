/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.github.util;

import se.kth.karamel.common.exception.KaramelException;

public class GithubUrl {

  public static String getProtocol(String githubUrl) throws KaramelException {
    if (githubUrl == null || githubUrl.isEmpty()) {
      throw new KaramelException("Misformed empty url: " + githubUrl);      
    }
    String protocol = githubUrl.substring(0,5);

    if (protocol.substring(0,4).compareToIgnoreCase("http") != 0 &&
        protocol.substring(0,4).compareToIgnoreCase("git@") != 0 &&
        protocol.compareToIgnoreCase("https") != 0 ) {
      throw new KaramelException("Misformed url - only 'http' and 'git@' supported: " + githubUrl);
    }

    return protocol.compareToIgnoreCase("https") == 0 ? protocol : protocol.substring(0,4);
  }

  public static String extractRepoName(String githubUrl) throws KaramelException {
    int e = githubUrl.lastIndexOf(".git");
    int s = githubUrl.lastIndexOf("/");
    if (s == -1 || e == -1) {
      throw new KaramelException("Misformed url: " + githubUrl);
    }
    String repoName = githubUrl.substring(s+1, e);
    if (repoName == null || repoName.isEmpty()) {
      throw new KaramelException("Misformed url repo/owner: " + githubUrl);
    }
    return repoName;
  }

  public static String extractUserName(String githubUrl) throws KaramelException {
    String protocol = getProtocol(githubUrl);

    int e = githubUrl.lastIndexOf(".git");
    int s = githubUrl.lastIndexOf("/");
    if (s == -1 || e == -1) {
      throw new KaramelException("Misformed url: " + githubUrl);
    }
    int s1 = -1;
    if (protocol.compareToIgnoreCase("http") == 0 || protocol.compareToIgnoreCase("https") == 0) {
      s1 = githubUrl.lastIndexOf("/", s-1);
    } else if (protocol.compareToIgnoreCase("git@") == 0) {
      s1 = githubUrl.lastIndexOf(":", s-1);
    } else {
      throw new KaramelException("Unrecognized protocol: " + protocol);
    }
    if (s1 == -1) {
      throw new KaramelException("Misformed url: " + githubUrl);
    }
    String owner = githubUrl.substring(s1+1, s);
    if (owner == null || owner.isEmpty()) {
      throw new KaramelException("Misformed url repo/owner: " + githubUrl);
    }
    return owner;
  }

}
