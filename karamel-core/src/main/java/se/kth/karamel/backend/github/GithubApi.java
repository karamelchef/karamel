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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
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
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.CookbookScaffolder;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 * 1. Call registerCredentials() to store your github credentials in memory. 2. Then call methods like addFile(),
 * commitPush(repo,..)
 *
 */
public class GithubApi {
  
  private static volatile String user = "";
  private static volatile String email = "";
  private static volatile String password = "";

  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GithubApi.class);

  private static final GitHubClient client = GitHubClient.createClient("http://github.com");

  private static final Map<String, List<OrgItem>> cachedOrgs = new HashMap<>();
  private static final Map<String, List<RepoItem>> cachedRepos = new HashMap<>();

  // Singleton
  private GithubApi() {
  }

  /**
   * Blindly accepts user credentials, no validation with github.
   *
   * @param user
   * @param password
   * @return primary github email for the user
   * @throws se.kth.karamel.common.exception.KaramelException
   */
  public synchronized static GithubUser registerCredentials(String user, String password) throws KaramelException {
    try {
      GithubApi.user = user;
      GithubApi.password = password;
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
      GithubApi.email = u.getEmail();
    } catch (IOException ex) {
      logger.warn("Problem connecting to GitHub: " + ex.getMessage());
    }
    return new GithubUser(GithubApi.user, GithubApi.password, GithubApi.email);
  }

  /**
   *
   * @return email or null if not set yet.
   */
  public static String getEmail() {
    return GithubApi.email;
  }

  public static GithubUser loadGithubCredentials() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    GithubApi.user = confs.getProperty(Settings.GITHUB_USER);
    GithubApi.password = confs.getProperty(Settings.GITHUB_PASSWORD);
    if (GithubApi.user != null && GithubApi.password != null) {
      registerCredentials(GithubApi.user, GithubApi.password);
    }
    return new GithubUser(GithubApi.user, GithubApi.password, GithubApi.email);
  }

  public synchronized static String getUser() {
    return GithubApi.user;
  }

  public synchronized static String getPassword() {
    return GithubApi.password;
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
    if (cachedOrgs.get(GithubApi.getUser()) != null) {
      return cachedOrgs.get(GithubApi.getUser());
    }
    try {
      List<String> orgs = new ArrayList<>();
      OrganizationService os = new OrganizationService(client);
      List<User> longOrgsList = os.getOrganizations();
      List<OrgItem> orgsList = new ArrayList<>();
      for (User u : longOrgsList) {
        orgsList.add(new OrgItem(u.getLogin(), u.getAvatarUrl()));
      }
      cachedOrgs.put(GithubApi.getUser(), orgsList);

      return orgsList;
    } catch (IOException ex) {
      throw new KaramelException("Problem listing GitHub organizations: " + ex.getMessage());
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
    if (cachedRepos.get(orgName) != null) {
      return cachedRepos.get(orgName);
    }

    try {
      RepositoryService rs = new RepositoryService(client);
      List<Repository> repos;
      // If we are looking for the repositories for the current user
      if (GithubApi.getUser().equalsIgnoreCase(orgName)) {
        repos = rs.getRepositories(orgName);
      } else {       // If we are looking for the repositories for a given organization
        repos = rs.getOrgRepositories(orgName);
      }

      List<RepoItem> repoItems = new ArrayList<>();
      for (Repository r : repos) {
        repoItems.add(new RepoItem(r.getName(), r.getDescription(), r.getSshUrl()));
      }
      cachedRepos.put(orgName, repoItems);
      return repoItems;
    } catch (IOException ex) {
      throw new KaramelException("Problem listing GitHub repositories: " + ex.getMessage());
    }
  }

  public synchronized static boolean repoExists(String owner, String repoName) throws KaramelException {
    List<RepoItem> repos = GithubApi.getRepos(owner);
    if (repos == null) {
      return false;
    }
    boolean found = false;
    for (RepoItem r : repos) {
      if (r.getName().compareToIgnoreCase(repoName) == 0) {
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * Gets local directory for a given repository name.
   *
   * @param repoName
   * @return File representing the local directory
   */
  public static File getRepoDirectory(String repoName) {
    File targetDir = new File(Settings.COOKBOOKS_PATH);
    if (targetDir.exists() == false) {
      targetDir.mkdirs();
    }
    return new File(Settings.COOKBOOKS_PATH + File.separator + repoName);
  }

  /**
   * Create a repository for a given organization with a description
   *
   * @param org
   * @param repoName
   * @param description
   * @return RepoItem bean/json object
   * @throws KaramelException
   */
  public synchronized static RepoItem createRepoForOrg(String org, String repoName, String description) throws
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
      cachedRepos.remove(org);
      return new RepoItem(repoName, description, r.getSshUrl());
    } catch (IOException ex) {
      throw new KaramelException("Problem creating the repository " + repoName + " for organization " + org
          + " : " + ex.getMessage());
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
      cloneRepo(getUser(), repoName);
      cachedRepos.remove(GithubApi.getUser());
    } catch (IOException ex) {
      throw new KaramelException("Problem creating " + repoName + " for user " + ex.getMessage());
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
      File localPath = new File(Settings.COOKBOOKS_PATH + File.separator + repoName);
      if (localPath.isDirectory() == false) {
        localPath.mkdirs();
      } else {
        throw new KaramelException("Local directory already exists. Delete it first: " + localPath);
      }

      logger.debug("Cloning from " + cloneURL + " to " + localPath);
      result = Git.cloneRepository()
          .setURI(cloneURL)
          .setDirectory(localPath)
          .call();
      // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
      logger.debug("Cloned repository: " + result.getRepository().getDirectory());
    } catch (IOException | GitAPIException ex) {
      throw new KaramelException("Problem cloning repo: " + ex.getMessage());
    } finally {
      if (result != null) {
        result.close();
      }
    }

  }

  public synchronized static void removeRepo(String owner, String repoName) throws KaramelException {

    try {
      GitHub gitHub = GitHub.connectUsingPassword(GithubApi.getUser(), GithubApi.getPassword());
      if (!gitHub.isCredentialValid()) {
        throw new KaramelException("Invalid GitHub credentials");
      }
      GHRepository repo = null;
      if (owner.compareToIgnoreCase(GithubApi.getUser()) != 0) {
        GHOrganization org = gitHub.getOrganization(owner);
        repo = org.getRepository(repoName);
      } else {
        repo = gitHub.getRepository(owner + "/" + repoName);
      }
      repo.delete();

    } catch (IOException ex) {
      throw new KaramelException("Problem authenticating with gihub-api when trying to remove a repository");
    }
  }

  public synchronized static void removeLocalRepo(String owner, String repoName) throws KaramelException {
    File path = getRepoDirectory(repoName);
    try {
      FileUtils.deleteDirectory(path);
    } catch (IOException ex) {
      throw new KaramelException("Couldn't find the path to delete for Repo: " + repoName + " with owner: " + owner);
    }
  }

  /**
   * Adds a file to the Github repo's index. If the file already exists, it will delete it and replace its contents with
   * the new contents. You wil subsequenty need to commit the change and push the commit to github.
   *
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
      new File(repoDir + File.separator + fileName).getParentFile().mkdirs();
      try (PrintWriter out = new PrintWriter(repoDir + File.separator + fileName)) {
        out.println(contents);
      }
      git.add().addFilepattern(fileName).call();
    } catch (IOException | GitAPIException ex) {
      throw new KaramelException(ex.getMessage());
    } finally {
      if (git != null) {
        git.close();
      }

    }
  }

  public synchronized static void removeFile(String owner, String repoName, String fileName)
      throws KaramelException {
    File repoDir = getRepoDirectory(repoName);
    Git git = null;
    try {
      git = Git.open(repoDir);
      new File(repoDir + File.separator + fileName).delete();
      git.add().addFilepattern(fileName).call();
      git.commit().setAuthor(user, email).setMessage("File removed by Karamel.")
          .setAll(true).call();
      git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, password)).call();
      RepoItem toRemove = null;
      List<RepoItem> repos = cachedRepos.get(owner);
      for (RepoItem r : repos) {
        if (r.getName().compareToIgnoreCase(repoName) == 0) {
          toRemove = r;
        }
      }
      if (toRemove != null) {
        repos.remove(toRemove);
      }
    } catch (IOException | GitAPIException ex) {
      throw new KaramelException(ex.getMessage());
    } finally {
      if (git != null) {
        git.close();
      }
    }
  }

  /**
   * Scaffolds a Karamel/chef project for an experiment and adds it to the github repo. You still need to commit and
   * push the changes to github.
   *
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
      throw new KaramelException("Problem scaffolding a new Repository: " + ex.getMessage());
    } finally {
      if (git != null) {
        git.close();
      }
    }
  }

  /**
   * Synchronizes your updates on your local repository with github.
   *
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
      logger.error("error during github push", ex);
      throw new KaramelException(ex.getMessage());
    } finally {
      if (git != null) {
        git.close();
      }

    }
  }

  /**
   * Search github for organizations/repositories/users using the GitHub API.
   *
   * @param query
   * @throws IOException
   */
  public synchronized static void searchRepos(String query) throws IOException {
    RepositoryService rs = new RepositoryService(client);

    List<SearchRepository> listRepos = rs.searchRepositories(query);
  }

}
