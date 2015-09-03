package se.kth.karamel.webservice;

import se.kth.karamel.webservice.calls.cluster.StartCluster;
import se.kth.karamel.webservice.utils.TemplateHealthCheck;
import icons.TrayUI;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.SystemTray;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.common.exception.KaramelException;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.EnumSet;
import java.util.List;
import javax.swing.ImageIcon;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.Experiment;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.backend.github.GithubUser;
import se.kth.karamel.backend.github.OrgItem;
import se.kth.karamel.backend.github.RepoItem;
import se.kth.karamel.client.model.yaml.YamlCluster;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.CookbookScaffolder;
import static se.kth.karamel.common.CookbookScaffolder.deleteRecursive;
import se.kth.karamel.webservice.calls.definition.JsonToYaml;
import se.kth.karamel.webservice.calls.definition.YamlToJson;
import se.kth.karamel.webservice.calls.sshkeys.GenerateSshKeys;
import se.kth.karamel.webservice.calls.sshkeys.LoadSshKeys;
import se.kth.karamel.webservice.calls.sshkeys.RegisterSshKeys;
import se.kth.karamel.webservicemodel.CommandJSON;
import se.kth.karamel.webservicemodel.CookbookJSON;
import se.kth.karamel.webservicemodel.KaramelBoardJSON;
import se.kth.karamel.webservicemodel.KaramelBoardYaml;

import se.kth.karamel.webservicemodel.Ec2JSON;
import se.kth.karamel.webservicemodel.GceJson;
import se.kth.karamel.webservicemodel.SshKeyJSON;
import se.kth.karamel.webservicemodel.StatusResponseJSON;
import se.kth.karamel.webservicemodel.SudoPasswordJSON;

public class KaramelServiceApplication extends Application<KaramelServiceConfiguration> {

  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
      KaramelServiceApplication.class);

  private static KaramelApi karamelApi;

  public static TrayUI trayUi;

  private TemplateHealthCheck healthCheck;

  private static final Options options = new Options();
  private static final CommandLineParser parser = new GnuParser();

  private static final int PORT = 58931;
  private static ServerSocket s;

  static {
// Ensure a single instance of the app is running
    try {
      s = new ServerSocket(PORT, 10, InetAddress.getLocalHost());
    } catch (UnknownHostException e) {
      // shouldn't happen for localhost
    } catch (IOException e) {
      // port taken, so app is already running
      logger.info("An instance of Karamel is already running. Exiting...");
      System.exit(10);
    }

    options.addOption("help", false, "Print help message.");
    options.addOption(OptionBuilder.withArgName("yamlFile")
        .hasArg()
        .withDescription("Dropwizard configuration in a YAML file")
        .create("server"));
    options.addOption(OptionBuilder.withArgName("yamlFile")
        .hasArg()
        .withDescription("Karamel cluster definition in a YAML file")
        .create("launch"));
    options.addOption("scaffold", false, "Creates scaffolding for a new Chef/Karamel Cookbook.");
  }

  public static void create() {
    String name = "";
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("Enter cookbook name: ");
      name = br.readLine();
      File cb = new File("cookbooks" + File.separator + name);
      if (cb.exists()) {
        boolean wiped = false;
        while (!wiped) {
          System.out.print("Do you wan  t to over-write the existing cookbook " + name + "? (y/n) ");
          String overwrite = br.readLine();
          if (overwrite.compareToIgnoreCase("n") == 0 || overwrite.compareToIgnoreCase("no") == 0) {
            logger.info("Not over-writing. Exiting.");
            System.exit(0);
          }
          if (overwrite.compareToIgnoreCase("y") == 0 || overwrite.compareToIgnoreCase("yes") == 0) {
            deleteRecursive(cb);
            wiped = true;
          }
        }
      }
      String pathToCb = CookbookScaffolder.create(name);
      logger.info("New Cookbook is now located at: " + pathToCb);
      System.exit(0);
    } catch (IOException ex) {
      logger.error("", ex);
      System.exit(-1);
    }
  }

  /**
   * Usage instructions
   *
   * @param exitValue
   */
  public static void usage(int exitValue) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("karamel", options);
    System.exit(exitValue);
  }

  public static void main(String[] args) throws Exception {

    System.setProperty("java.net.preferIPv4Stack", "true");
    boolean cli = false;
    String yamlTxt;

    // These args are sent to the Dropwizard app (thread)
    String[] modifiedArgs = new String[2];
    modifiedArgs[0] = "server";

    karamelApi = new KaramelApiImpl();

    try {
      CommandLine line = parser.parse(options, args);
      if (line.getOptions().length == 0) {
        usage(0);
      }
      if (line.hasOption("help")) {
        usage(0);
      }
      if (line.hasOption("scaffold")) {
        create();
      }
      if (line.hasOption("server")) {
        modifiedArgs[1] = line.getOptionValue("server");
      }
      if (line.hasOption("launch")) {
        cli = true;
      }

      if (cli) {
        // Try to open and read the yaml file. 
        // Print error msg if invalid file or invalid YAML.
        try {
          yamlTxt = CookbookScaffolder.readFile(line.getOptionValue("launch"));
          YamlCluster cluster = ClusterDefinitionService.yamlToYamlObject(yamlTxt);
          String jsonTxt = karamelApi.yamlToJson(yamlTxt);
          boolean valid = false;
          Ec2Credentials credentials = karamelApi.loadEc2CredentialsIfExist();

          Console c = null;
          if (credentials == null) {
            c = System.console();
            if (c == null) {
              System.err.println("No console available.");
              System.exit(1);
            }
          }
          String ec2AccountId = null;
          String ec2AccessKey = null;
          while (!valid) {
            if (ec2AccountId == null || ec2AccountId.isEmpty()) {
              ec2AccountId = c.readLine("Enter your Ec2 Access Key:");
            }
            if (ec2AccessKey == null || ec2AccessKey.isEmpty()) {
              char[] secretKeyChars = c.readPassword("Enter your Ec2 Secret Key:");
              ec2AccessKey = new String(secretKeyChars);
            }
            credentials = new Ec2Credentials();
            credentials.setAccessKey(ec2AccountId);
            credentials.setSecretKey(ec2AccessKey);
            valid = karamelApi.updateEc2CredentialsIfValid(credentials);
            if (!valid) {
              logger.info("Invalid Ec2 Credentials. Try again.");
              ec2AccountId = null;
              ec2AccessKey = null;
            }
          }
          karamelApi.startCluster(jsonTxt);

          long ms1 = System.currentTimeMillis();
          while (ms1 + 6000000 > System.currentTimeMillis()) {
            String clusterStatus = karamelApi.getClusterStatus(cluster.getName());
            logger.debug(clusterStatus);
            Thread.currentThread().sleep(30000);
          }
        } catch (KaramelException e) {
          System.err.println("Inalid yaml file; " + e.getMessage());
          System.exit(-1);
        } catch (IOException e) {
          System.err.println("Could not find or parse yaml file.");
          System.exit(-1);
        }
      }
    } catch (ParseException e) {
      usage(-1);
    }

    if (!cli) {
      new KaramelServiceApplication().run(modifiedArgs);
    }

    Runtime.getRuntime().addShutdownHook(new KaramelCleanupBeforeShutdownThread());
  }

// Name of the application displayed when application boots up.
  @Override
  public String getName() {
    return "karamel-core";
  }

  // Pre start of the dropwizard to plugin with separate bundles.
  @Override
  public void initialize(Bootstrap<KaramelServiceConfiguration> bootstrap) {

    logger.debug("Executing any initialization tasks.");
//        bootstrap.addBundle(new ConfiguredAssetsBundle("/assets/", "/dashboard/"));
    // https://groups.google.com/forum/#!topic/dropwizard-user/UaVcAYm0VlQ
    bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
  }

  @Override
  public void run(KaramelServiceConfiguration configuration, Environment environment) throws Exception {

    healthCheck = new TemplateHealthCheck("%s");
//        http://stackoverflow.com/questions/26610502/serve-static-content-from-a-base-url-in-dropwizard-0-7-1
//        environment.jersey().setUrlPattern("/angular/*");

    /*
     * To allow cross orign resource request from angular js client
     */
    FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class
    );

    // Allow cross origin requests.
    filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class
    ), true, "/*");
    filter.setInitParameter(
        "allowedOrigins", "*"); // allowed origins comma separated
    filter.setInitParameter(
        "allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
    filter.setInitParameter(
        "allowedMethods", "GET,PUT,POST,DELETE,OPTIONS,HEAD");
    filter.setInitParameter(
        "preflightMaxAge", "5184000"); // 2 months
    filter.setInitParameter(
        "allowCredentials", "true");

    environment.jersey()
        .setUrlPattern("/api/*");

    environment.healthChecks()
        .register("template", healthCheck);

    environment.jersey().register(new YamlToJson(karamelApi));
    environment.jersey().register(new JsonToYaml(karamelApi));
    environment.jersey().register(new Cookbook());
    environment.jersey().register(new LoadSshKeys(karamelApi));
    environment.jersey().register(new RegisterSshKeys(karamelApi));
    environment.jersey().register(new GenerateSshKeys(karamelApi));
    environment.jersey().register(new Ec2.Load());
    environment.jersey().register(new Ec2.Validate());
    environment.jersey().register(new Gce.Load());
    environment.jersey().register(new Gce.Validate());
    environment.jersey().register(new StartCluster(karamelApi));
    environment.jersey().register(new Command.CheatSheet());
    environment.jersey().register(new Command.Process());
    environment.jersey().register(new ExitKaramel());
    environment.jersey().register(new PingKaramel());
    environment.jersey().register(new Sudo.SudoPassword());
    environment.jersey().register(new Github.GetGithubCredentials());
    environment.jersey().register(new Github.SetGithubCredentials());
    environment.jersey().register(new Github.LoadExperiment());
    environment.jersey().register(new Github.GetGithubOrgs());
    environment.jersey().register(new Github.GetGithubRepos());
    environment.jersey().register(new Github.PushExperiment());
    environment.jersey().register(new Github.RemoveFileFromExperiment());
    environment.jersey().register(new Github.RemoveRepository());

    // Wait to make sure jersey/angularJS is running before launching the browser
    final int webPort = getPort(environment);

    if (SystemTray.isSupported()) {
      trayUi = new TrayUI(createImage("if.png", "tray icon"), getPort(environment));
    }
    new Thread("webpage opening..") {
      public void run() {
        try {
          Thread.sleep(1500);
          openWebpage(new URL("http://localhost:" + webPort + "/index.html#/"));
        } catch (InterruptedException e) {
//           swallow the exception
        } catch (java.net.MalformedURLException e) {
          // swallow the exception
        }
      }
    }.start();

  }

  protected static Image createImage(String path, String description) {
    URL imageURL = TrayUI.class.getResource(path);

    if (imageURL == null) {
      System.err.println("Resource not found: " + path);
      return null;
    } else {
      return (new ImageIcon(imageURL, description)).getImage();
    }
  }

  public int getPort(Environment environment) {
    int defaultPort = 9090;
    MutableServletContextHandler h = environment.getApplicationContext();
    if (h == null) {
      return defaultPort;
    }
    Server s = h.getServer();
    if (s == null) {
      return defaultPort;
    }
    Connector[] c = s.getConnectors();
    if (c != null && c.length > 0) {
      AbstractNetworkConnector anc = (AbstractNetworkConnector) c[0];
      if (anc != null) {
        return anc.getLocalPort();
      }
    }
    return defaultPort;
  }

  public synchronized static void openWebpage(URI uri) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(uri);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("Brower UI could not be launched using Java's Desktop library. "
          + "Are you running a window manager?");
      System.err.println("If you are using Ubuntu, try: sudo apt-get install libgnome");
      System.err.println("Retrying to launch the browser now using a different method.");
      BareBonesBrowserLaunch.openURL(uri.toASCIIString());
    }
  }

  public static void openWebpage(URL url) {
    try {
      openWebpage(url.toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  @Path("/fetchCookbook")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public static class Cookbook {

    @PUT
    public Response getCookbook(CookbookJSON cookbookJSON) {
      Response response = null;
      try {
        String cookbookDetails = karamelApi.getCookbookDetails(cookbookJSON.getUrl(), cookbookJSON.isRefresh());
        response = Response.status(Response.Status.OK).entity(cookbookDetails).build();

      } catch (KaramelException e) {
        e.printStackTrace();
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
            entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
      }

      return response;
    }
  }

  public static class Command {

    @Path("/getCommandCheetSheet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class CheatSheet {

      @PUT
      public Response getCommandCheetSheet() {
        Response response = null;
        logger.debug("Received request to load the command cheatsheet.");
        try {
          String cheatSheet = karamelApi.commandCheatSheet();
          response = Response.status(Response.Status.OK).entity(cheatSheet).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }
        return response;
      }
    }

    @Path("/processCommand")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class Process {

      @PUT
      public Response processCommand(CommandJSON command) {
        Response response = null;

        logger.debug("Received request to process a command with info: " + command.getCommand());
        try {
          CommandResponse cmdRes = karamelApi.processCommand(command.getCommand(), command.getResult());
          command.setResult(cmdRes.getResult());
          command.setNextCmd(cmdRes.getNextCmd());
          command.setRenderer(cmdRes.getRenderer().name().toLowerCase());
          command.getMenuItems().addAll(cmdRes.getMenuItems());
          command.setSuccessmsg(cmdRes.getSuccessMessage());
          command.setContext(cmdRes.getContext());
        } catch (KaramelException e) {
          command.setErrormsg(e.getMessage());
        } catch (Exception e) {
          command.setErrormsg(e.getMessage());
        } finally {
          response = Response.status(Response.Status.OK).entity(command).build();
        }
        return response;
      }
    }
  }

  public static class Ec2 {

    @Path("/ec2/loadCredentials")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class Load {

      @PUT
      public Response loadCredentials() {
        Response response = null;
        logger.debug("Received request to load the ec2 credentials.");
        try {
          Ec2Credentials credentials = karamelApi.loadEc2CredentialsIfExist();
          Ec2JSON provider = new Ec2JSON();
          provider.setAccountId((credentials == null) ? "" : credentials.getAccessKey());
          provider.setAccountKey((credentials == null) ? "" : credentials.getSecretKey());
          response = Response.status(Response.Status.OK).entity(provider).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }
        return response;
      }
    }

    @Path("/ec2/validateCredentials")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class Validate {

      /**
       * Validating the Provider based on the supplied credentials..
       *
       * @param providerJSON
       * @return
       */
      @PUT
      public Response validateCredentials(Ec2JSON providerJSON) {

        Response response = null;
        logger.debug("Received request to validate the ec2 credentials.");

        try {
          Ec2Credentials credentials = new Ec2Credentials();
          credentials.setAccessKey(providerJSON.getAccountId());
          credentials.setSecretKey(providerJSON.getAccountKey());
          if (karamelApi.updateEc2CredentialsIfValid(credentials)) {
            response = Response.status(Response.Status.OK).
                entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
          } else {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, "Invalid Credentials")).build();
          }

        } catch (KaramelException e) {
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }
        return response;
      }
    }
  }

  public static class Gce {

    @Path("/gce/loadCredentials")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class Load {

      @PUT
      public Response loadCredentials() {
        Response response = null;
        logger.debug("Received request to load the gce credentials.");
        try {
          String jsonKeyPath = karamelApi.loadGceCredentialsIfExist();
          GceJson provider = new GceJson();
          provider.setJsonKeyPath((jsonKeyPath == null) ? "" : jsonKeyPath);
          response = Response.status(Response.Status.OK).entity(provider).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }
        return response;
      }
    }

    @Path("/gce/validateCredentials")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class Validate {

      /**
       * Validating the Provider based on the supplied credentials..
       *
       * @param providerJSON
       * @return
       */
      @PUT
      public Response validateCredentials(GceJson providerJSON) {

        Response response = null;
        logger.debug("Received request to validate the gce credentials.");

        try {
          String jsonKeyPath = providerJSON.getJsonKeyPath();
          if (karamelApi.updateGceCredentialsIfValid(jsonKeyPath)) {
            response = Response.status(Response.Status.OK).
                entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
          } else {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, "Invalid Credentials")).build();
          }

        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }
        return response;
      }
    }
  }


  @Path("/exitKaramel")
  public static class ExitKaramel {

    @GET
    public Response exitKaramel() {
      Response response = Response.status(Response.Status.OK).build();

      new Thread() {
        @Override
        public void run() {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException ex) {
            logger.warn(ex.getMessage());
          } finally {
            logger.info("Karamel Shutdown finished.");
            System.exit(0);
          }
        }
      }.start();

      return response;
    }
  }

  @Path("/ping")
  public static class PingKaramel {

    @GET
    public Response pingKaramel() {
      Response response = Response.status(Response.Status.OK).build();
      return response;
    }
  }

  public static class Sudo {

    @Path("/sudoPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class SudoPassword {

      @PUT
      public Response sudoPassword(SudoPasswordJSON sudoPwd) {
        Response response = null;
        logger.info(" Received request to set sudo password....");
        try {
          karamelApi.registerSudoPassword(sudoPwd.getPassword());
          response = Response.status(Response.Status.OK).
              entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }
        return response;
      }
    }
  }

  public static class Github {

    @Path("/getGithubCredentials")
    @Produces(MediaType.APPLICATION_JSON)
    public static class GetGithubCredentials {

      @GET
      public Response getGithubCredentials() {
        Response response = null;
        logger.info(" Received request to get github credentials.... ");
        try {
          GithubUser credentials = karamelApi.loadGithubCredentials();
          response = Response.status(Response.Status.OK).
              entity(credentials).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }
        return response;
      }

    }

    @Path("/setGithubCredentials")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public static class SetGithubCredentials {

      @POST
      public Response setGithubCredentials(@FormParam("user") String user, @FormParam("password") String password) {
        Response response = null;
        logger.info(" Received request to set github credentials.... ");
        try {
          GithubUser githubUser = karamelApi.registerGithubAccount(user, password);

          response = Response.status(Response.Status.OK).
              entity(githubUser).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }

        return response;
      }
    }

    @Path("/getGithubOrgs")
    @Produces(MediaType.APPLICATION_JSON)
    public static class GetGithubOrgs {

      @POST
      public Response getGithubOrgs() {
        Response response = null;
        logger.info(" Received request to set github credentials.... ");
        try {
          List<OrgItem> orgs = karamelApi.listGithubOrganizations();
          response = Response.status(Response.Status.OK).
              entity(orgs).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }

        return response;
      }
    }

    @Path("/getGithubRepos")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public static class GetGithubRepos {

      @POST
      public Response getGithubRepos(@FormParam("org") String org) {
        Response response = null;
        logger.info(" Received request to set github credentials.... ");
        try {
          List<RepoItem> repos = karamelApi.listGithubRepos(org);
          response = Response.status(Response.Status.OK).
              entity(repos).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }

        return response;
      }
    }


    @Path("/pushExperiment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class PushExperiment {

      @PUT
      public Response pushExperiment(Experiment experiment) {
        Response response = null;
        logger.info(" Received request to set github credentials.... ");
        try {
          karamelApi.commitAndPushExperiment(experiment);
          response = Response.status(Response.Status.OK).
              entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }

        return response;
      }
    }

    @Path("/loadExperiment")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public static class LoadExperiment {

      @POST
      public Response loadExperiment(@FormParam("experimentUrl") String experimentUrl) {
        Response response = null;
        logger.info(" Received request to set github credentials.... ");
        try {
          Experiment ec = karamelApi.loadExperiment(experimentUrl);
          response = Response.status(Response.Status.OK).entity(ec).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }

        return response;
      }
    }

    @Path("/removeFileFromExperiment")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public static class RemoveFileFromExperiment {

      @POST
      public Response removeFileFromExperiment(@FormParam("org") String org, @FormParam("repo") String repo,
          @FormParam("filename") String filename) {
        logger.info(" Received request to set github credentials.... ");
        karamelApi.removeFileFromExperiment(org, repo, filename);
        return Response.status(Response.Status.OK).entity(
            new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
      }
    }

    @Path("/removeRepository")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public static class RemoveRepository {

      @POST
      public Response removeRepository(@FormParam("org") String org, @FormParam("repo") String repo,
          @FormParam("local") boolean local, @FormParam("remote") boolean remote) {
        Response response = null;
        logger.info(" Received request to set github credentials.... ");
        try {
          karamelApi.removeRepo(org, repo, local, remote);
          response = Response.status(Response.Status.OK).
              entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }
        return response;
      }
    }
  }

  static class KaramelCleanupBeforeShutdownThread extends Thread {

    @Override
    public void run() {
      logger.info("Bye! Cleaning up first....");
      // TODO - interrupt all threads
      // Should we cleanup AMIs?
    }
  }

}
