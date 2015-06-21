/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.github;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.CookbookScaffolder;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 * 1. Call registerCredentials() to store your github credentials in memory.
 * 2. Then call methods like addFile(), commitPush(repo,..)
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
   * @throws se.kth.karamel.common.exception.KaramelException
   */
  public synchronized static void registerCredentials(String user, String password) throws KaramelException {
    try {
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
        throw new KaramelException("Could not find user or password incorret: " + user);
      }
      User u = us.getUser();
      if (u == null) {
        throw new KaramelException("Could not find user or password incorret: " + user);
      }
      email = u.getEmail();
    } catch (IOException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   *
   * @return email or null if not set yet.
   */
  public static String getEmail() {
    return email;
  }

  public static GithubUser loadGithubCredentials() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    Github.user = confs.getProperty(Settings.GITHUB_USER);
    Github.password = confs.getProperty(Settings.GITHUB_PASSWORD);
    if (Github.user != null && Github.password != null) {
      registerCredentials(Github.user, Github.password);
    }
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

  /**
   *
   * @return List of github orgs for authenticated user
   * @throws KaramelException
   */
  public synchronized static List<OrgItem> getOrganizations() throws KaramelException {
    try {
      List<String> orgs = new ArrayList<>();
      OrganizationService os = new OrganizationService(client);
      List<User> longOrgsList = os.getOrganizations();
      List<OrgItem> orgsList = new ArrayList<>();
      for (User u : longOrgsList) {
        orgsList.add(new OrgItem(u.getLogin(), u.getAvatarUrl()));
      }
      return orgsList;
    } catch (IOException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());

    }
  }

  /**
   * Gets all repositories for a given organization/user.
   *
   * @param orgName
   * @return List of repositories
   * @throws KaramelException
   */
  public synchronized static List<RepoItem> getRepos(String orgName) throws KaramelException {
    try {
      RepositoryService rs = new RepositoryService(client);
      List<Repository> repos = rs.getOrgRepositories(orgName);
      List<RepoItem> repoItems = new ArrayList<>();
      for (Repository r : repos) {
        repoItems.add(new RepoItem(r.getName(), r.getDescription(), r.getSshUrl()));
      }
      return repoItems;
    } catch (IOException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    }
  }

  /**
   * Gets local directory for a given repository name.
   *
   * @param repoName
   * @return File representing the local directory
   */
  public static File getRepoDirectory(String repoName) {
    File targetDir = new File(Settings.COOKBOOK_DESIGNER_PATH);
    if (targetDir.exists() == false) {
      targetDir.mkdirs();
    }
    return new File(Settings.COOKBOOK_DESIGNER_PATH + File.separator + repoName);
  }

  /**
   * Create a repository for a given organization with a description
   *
   * @param org
   * @param repoName
   * @param description
   * @throws KaramelException
   */
  public synchronized static void createRepoForOrg(String org, String repoName, String description) throws
      KaramelException {

    try {
      OrganizationService os = new OrganizationService(client);
      RepositoryService rs = new RepositoryService(client);
      Repository r = new Repository();
      r.setName(repoName);
      r.setOwner(os.getOrganization(org));
      r.setDescription(description);
      rs.createRepository(org, r);
      cloneRepo(org, repoName);
    } catch (IOException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    }
  }

  /**
   * Create a repository in a given github user's local account.
   *
   * @param repoName
   * @param description
   * @throws KaramelException
   */
  public synchronized static void createRepoForUser(String repoName, String description) throws KaramelException {
    try {
      UserService us = new UserService(client);
      RepositoryService rs = new RepositoryService(client);
      Repository r = new Repository();
      r.setName(repoName);
      r.setOwner(us.getUser());
      r.setDescription(description);
      rs.createRepository(r);
      cloneRepo(us.getUser().getName(), repoName);
    } catch (IOException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    }
  }

  /**
   * Clone an existing github repo.
   *
   * @param owner
   * @param repoName
   * @throws se.kth.karamel.common.exception.KaramelException
   */
  public synchronized static void cloneRepo(String owner, String repoName) throws KaramelException {
    Git result = null;
    try {
      RepositoryService rs = new RepositoryService(client);
      Repository r = rs.getRepository(owner, repoName);

      String cloneURL = r.getSshUrl();
      // prepare a new folder for the cloned repository
      File localPath = new File(Settings.COOKBOOK_DESIGNER_PATH + File.separator + repoName);
      if (localPath.isDirectory() == false) {
        localPath.mkdirs();
      } else {
        throw new KaramelException("Local directory already exists. Delete it first: " + localPath);
      }

      System.out.println("Cloning from " + cloneURL + " to " + localPath);
      result = Git.cloneRepository()
          .setURI(cloneURL)
          .setDirectory(localPath)
          .call();
      // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
      System.out.println("Cloned repository: " + result.getRepository().getDirectory());
    } catch (IOException | GitAPIException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    } finally {
      if (result != null) {
        result.close();
      }
    }

  }

  /**
   * Adds a file to the Github repo's index. You then need to commit the change and push the commit to github. 
   * @param owner
   * @param repoName
   * @param fileName
   * @param contents
   * @throws KaramelException 
   */
  public synchronized static void addFile(String owner, String repoName, String fileName, String contents)
      throws KaramelException {
    File repoDir = getRepoDirectory(repoName);
    Git git = null;
    try {
      git = Git.open(repoDir);

      new File(repoDir + File.separator + fileName).delete();
      try (PrintWriter out = new PrintWriter(repoDir + File.separator + fileName)) {
        out.println(contents);
      }
      git.add().addFilepattern(fileName).call();
    } catch (IOException | GitAPIException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    } finally {
      if (git != null) {
        git.close();
      }

    }
  }

  /**
   * Scaffolds a Karamel/chef project for an experiment and adds it to the github repo.
   * You still need to commit and push the changes to github.
   * @param repoName
   * @throws KaramelException 
   */
  public static void scaffoldRepo(String repoName) throws KaramelException {
    File repoDir = getRepoDirectory(repoName);

    Git git = null;
    try {
      git = Git.open(repoDir);

      CookbookScaffolder.create(repoName);

      git.add().addFilepattern("Berksfile").addFilepattern("metadata.rb")
          .addFilepattern("Karamelfile")
          .addFilepattern(".kitchen.yml").addFilepattern("attributes").addFilepattern("recipes")
          .addFilepattern("templates").addFilepattern("README.md").call();

    } catch (IOException | GitAPIException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    } finally {
      if (git != null) {
        git.close();
      }
    }
  }

  /**
   * Synchronizes your updates on your local repository with github.
   * @param owner
   * @param repoName
   * @throws KaramelException 
   */
  public synchronized static void commitPush(String owner, String repoName)
      throws KaramelException {
    if (email == null || user == null) {
      throw new KaramelException("You forgot to call registerCredentials. You must call this method first.");
    }
    File repoDir = getRepoDirectory(repoName);
    Git git = null;
    try {
      git = Git.open(repoDir);

      git.commit().setAuthor(user, email).setMessage("Code generated by Karamel.")
          .setAll(true).call();
      git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, password)).call();
    } catch (IOException | GitAPIException ex) {
      Logger.getLogger(Github.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    } finally {
      if (git != null) {
        git.close();
      }

    }
  }

  /**
   * Search github for organizations/repositories/users using the GitHub API.
   * @param query
   * @throws IOException 
   */
  public synchronized static void searchRepos(String query) throws IOException {
    RepositoryService rs = new RepositoryService(client);

    List<SearchRepository> listRepos = rs.searchRepositories(query);
  }

}
