/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.github;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author jdowling
 */
public class Github {

  private static volatile String user;
  private static volatile String password;

  private static final GitHubClient client = GitHubClient.createClient("http://github.com");

  // Singleton
  private Github() {
  }

  /**
   * Blindly accepts user credentials, no validation with github.
   *
   * @param user
   * @param password
   */
  public synchronized static void registerCredentials(String user, String password) {
    Github.user = user;
    Github.password = password;
    client.setCredentials(user, password);
    client.getUser();
    Confs confs = Confs.loadKaramelConfs();
    confs.put(Settings.GITHUB_USER, user);
    confs.put(Settings.GITHUB_PASSWORD, password);
    confs.writeKaramelConfs();
  }

  public static GithubUser loadGithubCredentials() {
    Confs confs = Confs.loadKaramelConfs();
    Github.user = confs.getProperty(Settings.GITHUB_USER);
    Github.password = confs.getProperty(Settings.GITHUB_PASSWORD);
    return new GithubUser(Github.user, Github.password);
  }

  public synchronized static String getUser() {
    return Github.user;
  }

  public synchronized static String getPassword() {
    return Github.password;
  }

  public synchronized static int getRemainingRequests() {
    return client.getRemainingRequests();
  }

  public synchronized static int getRequestLimit() {
    return client.getRequestLimit();
  }

  public synchronized static List<OrgItem> getOrganizations() throws IOException {
    List<String> orgs = new ArrayList<>();
    OrganizationService os = new OrganizationService(client);
    List<User> longOrgsList = os.getOrganizations();
    List<OrgItem> orgsList = new ArrayList<>();
    for (User u : longOrgsList) {
      orgsList.add(new OrgItem(u.getLogin(), u.getAvatarUrl()));
    }
    return orgsList;
  }

  public synchronized static List<RepoItem> getRepos(String orgName) throws IOException {
    RepositoryService rs = new RepositoryService(client);
    List<Repository> repos = rs.getOrgRepositories(orgName);
    List<RepoItem> repoItems = new ArrayList<>();
    for (Repository r : repos) {
      repoItems.add(new RepoItem(r.getName(), r.getDescription(), r.getSshUrl()));
    }
    return repoItems;
  }

  public synchronized static void createRepoForOrg(String org, String repoName, String description) throws IOException {

    OrganizationService os = new OrganizationService(client);
    RepositoryService rs = new RepositoryService(client);
    Repository r = new Repository();
    r.setName(repoName);
    r.setOwner(os.getOrganization(org));
    r.setDescription(description);
    rs.createRepository(org, r);
  }

  public synchronized static void createRepoForUser(String repoName, String description) throws IOException {
    UserService us = new UserService(client);
    RepositoryService rs = new RepositoryService(client);
    Repository r = new Repository();
    r.setName(repoName);
    r.setOwner(us.getUser());
    r.setDescription(description);
    rs.createRepository(r);
  }

  public synchronized static void checkoutRepo(String owner, String name) throws IOException {
    RepositoryService rs = new RepositoryService(client);

    Repository r = rs.getRepository(owner, name);
  }

  public synchronized static void cloneRepo(String owner, String name) throws GitAPIException, IOException {
    RepositoryService rs = new RepositoryService(client);
    Repository r = rs.getRepository(owner, name);

    String cloneURL = r.getSshUrl();
    // prepare a new folder for the cloned repository
    File localPath = new File(Settings.COOKBOOK_DESIGNER_PATH + File.separator + name);
    if (localPath.isDirectory() == false) {
      localPath.mkdirs();
    } else {
      throw new IOException("Local directory already exists. Delete it first: " + localPath);
    }

    Git result = null;
    try {
      System.out.println("Cloning from " + cloneURL + " to " + localPath);
      result = Git.cloneRepository()
          .setURI(cloneURL)
          .setDirectory(localPath)
          .call();
      // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
      System.out.println("Cloned repository: " + result.getRepository().getDirectory());
    } finally {
      if (result != null) {
        result.close();
      }

    }

  }

  public synchronized static void commit(String owner, String name) throws IOException {
    RepositoryService rs = new RepositoryService(client);
    Repository r = rs.getRepository(owner, name);
    DataService ds = new DataService(client);
//    IRepositoryIdProvider irp = r.get 
    
    CommitService cs = new CommitService(client);

  }

  public synchronized static void searchRepos(String query) throws IOException {
    RepositoryService rs = new RepositoryService(client);

    List<SearchRepository> listRepos = rs.searchRepositories(query);
  }

}
