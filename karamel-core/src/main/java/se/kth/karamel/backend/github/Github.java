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
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import se.kth.karamel.backend.ExperimentContext;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.CookbookScaffolder;
import se.kth.karamel.common.Settings;

/**
 *
 * @author jdowling
 */
public class Github {

  private static volatile String user;
  private static volatile String email;
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
   * @throws java.io.IOException
   */
  public synchronized static void registerCredentials(String user, String password) throws IOException {
    Github.user = user;
    Github.password = password;
    client.setCredentials(user, password);
    client.getUser();
    Confs confs = Confs.loadKaramelConfs();
    confs.put(Settings.GITHUB_USER, user);
    confs.put(Settings.GITHUB_PASSWORD, password);
    confs.writeKaramelConfs();
    UserService us = new UserService(client);
    if (us == null) {
      throw new IOException("Could not find user or password incorret: " + user);
    }
    User u = us.getUser();
    if (u == null) {
      throw new IOException("Could not find user or password incorret: " + user);
    }
    email = u.getEmail();
  }

  /**
   *
   * @return email or null if not set yet.
   */
  public static String getEmail() {
    return email;
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

  public static File getRepoDirectory(String repoName) {
    File targetDir = new File(Settings.COOKBOOK_DESIGNER_PATH);
    if (targetDir.exists() == false) {
      targetDir.mkdirs();
    }
    return new File(Settings.COOKBOOK_DESIGNER_PATH + File.separator + repoName);
  }

  public synchronized static void createRepoForOrg(String org, String repoName, String description) throws IOException,
      GitAPIException {

    OrganizationService os = new OrganizationService(client);
    RepositoryService rs = new RepositoryService(client);
    Repository r = new Repository();
    r.setName(repoName);
    r.setOwner(os.getOrganization(org));
    r.setDescription(description);
    r = rs.createRepository(org, r);
    cloneRepo(org, repoName);
    addCommitPushRepo(repoName);
  }

  public synchronized static void createRepoForUser(String repoName, String description) throws IOException,
      GitAPIException {
    UserService us = new UserService(client);
    RepositoryService rs = new RepositoryService(client);
    Repository r = new Repository();
    r.setName(repoName);
    r.setOwner(us.getUser());
    r.setDescription(description);
    rs.createRepository(r);
    cloneRepo(us.getUser().getName(), repoName);
    addCommitPushRepo(repoName);
  }

  public synchronized static void checkoutRepo(String owner, String repoName) throws IOException {
    RepositoryService rs = new RepositoryService(client);

    Repository r = rs.getRepository(owner, repoName);
  }

  public synchronized static void cloneRepo(String owner, String repoName) throws GitAPIException, IOException {
    RepositoryService rs = new RepositoryService(client);
    Repository r = rs.getRepository(owner, repoName);

    String cloneURL = r.getSshUrl();
    // prepare a new folder for the cloned repository
    File localPath = new File(Settings.COOKBOOK_DESIGNER_PATH + File.separator + repoName);
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

//  public synchronized static void addCommitPushFile(String owner, String repoName, String fileName, String contents)
//      throws IOException {
//    File repoDir = getRepoDirectory(repoName);
//    Git git = null;
//    try {
//      git = Git.open(repoDir);
//
//      new File(repoDir + File.separator + fileName).delete();
//      try (PrintWriter out = new PrintWriter(repoDir + File.separator + fileName)) {
//        out.println(contents);
//      }
//      git.add().addFilepattern(fileName).call();
//      git.commit().setAuthor("Karamel", email).setMessage("Autogenerated file by Karamel")
//          .setAll(true).call();
//      git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, password)).call();
//    } catch (GitAPIException ex) {
//      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
//      throw new IOException(ex.getMessage());
//    } finally {
//      if (git != null) {
//        git.close();
//      }
//
//    }
//  }

  private static void addCommitPushRepo(String repoName, String... extraFiles) throws IOException {
    File repoDir = getRepoDirectory(repoName);

    Git git = null;
    try {
      git = Git.open(repoDir);

      CookbookScaffolder.create(repoName);

      AddCommand adder = git.add().addFilepattern("Berksfile").addFilepattern("metadata.rb").addFilepattern("Karamelfile")
          .addFilepattern(".kitchen.yml").addFilepattern("attributes").addFilepattern("recipes")
          .addFilepattern("templates").addFilepattern("README.md");
      
      for (String s : extraFiles) {
        adder.addFilepattern(s);
      }
      adder.call();
      git.commit().setAuthor("Karamel", email).setMessage("Autogenerated cookbook by Karamel")
          .setAll(true).call();
      git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, password)).call();
    } catch (GitAPIException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
      throw new IOException(ex.getMessage());
    } finally {
      if (git != null) {
        git.close();
      }

    }

  }

  public synchronized static void updateExperiment(String owner, String repoName, ExperimentContext experiment) throws
      IOException {

  }

  public synchronized static void searchRepos(String query) throws IOException {
    RepositoryService rs = new RepositoryService(client);

    List<SearchRepository> listRepos = rs.searchRepositories(query);
  }

}
