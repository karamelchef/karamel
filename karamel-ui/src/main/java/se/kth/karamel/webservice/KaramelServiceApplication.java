package se.kth.karamel.webservice;

import icons.TrayUI;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.cli.*;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.client.model.yaml.YamlCluster;
import se.kth.karamel.common.CookbookScaffolder;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservicemodel.*;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.swing.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static se.kth.karamel.common.CookbookScaffolder.deleteRecursive;

/**
 * Created by babbarshaer on 2014-11-20.
 */
public class KaramelServiceApplication extends Application<KaramelServiceConfiguration> {

  private static KaramelApi karamelApiHandler;

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
      System.out.println("An instance of Karamel is already running. Exiting...");
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
            System.out.println("Not over-writing. Exiting.");
            System.exit(0);
          }
          if (overwrite.compareToIgnoreCase("y") == 0 || overwrite.compareToIgnoreCase("yes") == 0) {
            deleteRecursive(cb);
            wiped = true;
          }
        }
      }
      String pathToCb = CookbookScaffolder.create(name);
      System.out.println("New Cookbook is now located at: " + pathToCb);
      System.out.println();
      System.exit(0);
    } catch (IOException ex) {
      Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.SEVERE, null, ex);
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

    karamelApiHandler = new KaramelApiImpl();

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
          String jsonTxt = karamelApiHandler.yamlToJson(yamlTxt);
          boolean valid = false;
          Ec2Credentials credentials = karamelApiHandler.loadEc2CredentialsIfExist();

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
            valid = karamelApiHandler.updateEc2CredentialsIfValid(credentials);
            if (!valid) {
              Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.WARNING,
                  "Invalid Ec2 Credentials. Try again.");
              ec2AccountId = null;
              ec2AccessKey = null;
            }
          }
          karamelApiHandler.startCluster(jsonTxt);

          long ms1 = System.currentTimeMillis();
          while (ms1 + 6000000 > System.currentTimeMillis()) {
            String clusterStatus = karamelApiHandler.getClusterStatus(cluster.getName());
            System.out.println(clusterStatus);
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

    System.out.println("Executing any initialization tasks.");
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

    environment.jersey().register(new ConvertYamlToJSON());
    environment.jersey().register(new ConvertJSONToYaml());
    environment.jersey().register(new Cookbook());
    environment.jersey().register(new Ssh.Load());
    environment.jersey().register(new Ssh.Register());
    environment.jersey().register(new Ssh.Generate());
    environment.jersey().register(new Ec2.Load());
    environment.jersey().register(new Ec2.Validate());
    environment.jersey().register(new Cluster.StartCluster());
    environment.jersey().register(new Scaffolder.ScaffoldCluster());
    environment.jersey().register(new Command.CheatSheet());
    environment.jersey().register(new Command.Process());
    environment.jersey().register(new ExitKaramel());
    environment.jersey().register(new Sudo.SudoPassword());
    environment.jersey().register(new Github.GithubCredentials());

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
      se.kth.karamel.webservice.BareBonesBrowserLaunch.openURL(uri.toASCIIString());
    }
  }

  public static void openWebpage(URL url) {
    try {
      openWebpage(url.toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  /**
   * For the cluster yml supplied by the UI, convert it into JSON Object and return.
   */
  @Path("/fetchJson")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public static class ConvertYamlToJSON {

    @PUT
    public Response getJSONForYaml(KaramelBoardYaml cluster) {

      Response response = null;
      try {
        String jsonClusterString = karamelApiHandler.yamlToJson(cluster.getYml());
        response = Response.status(Response.Status.OK).entity(jsonClusterString).build();
      } catch (KaramelException e) {
        e.printStackTrace();
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
            entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
      }
      return response;
    }
  }

  @Path("/fetchYaml")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public static class ConvertJSONToYaml {

    @PUT
    public Response getYamlForJSON(KaramelBoardJSON karamelBoardJSON) {
      Response response = null;
      Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.INFO, "Fetch Yaml Called ... ");

      try {
        String yml = karamelApiHandler.jsonToYaml(karamelBoardJSON.getJson());
        KaramelBoardYaml karamelBoardYaml = new KaramelBoardYaml(yml);
        response = Response.status(Response.Status.OK).entity(karamelBoardYaml).build();

      } catch (KaramelException e) {
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
            entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        e.printStackTrace();
      }
      return response;
    }
  }

  @Path("/fetchCookbook")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public static class Cookbook {

    @PUT
    public Response getCookbook(CookbookJSON cookbookJSON) {
      Response response = null;

      Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.INFO, "Received Call For the cookbook.");
      Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.INFO, cookbookJSON.getUrl());
      try {
        String cookbookDetails = karamelApiHandler.getCookbookDetails(cookbookJSON.getUrl(), cookbookJSON.isRefresh());
        response = Response.status(Response.Status.OK).entity(cookbookDetails).build();

      } catch (KaramelException e) {
        e.printStackTrace();
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
            entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
      }

      return response;
    }
  }

  public static class Ssh {

    @Path("/loadSshKeys")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class Load {

      @PUT
      public Response loadSshKeys() {
        Response response = null;
        Logger.getLogger(KaramelServiceApplication.class.getName()).
            log(Level.INFO, " Received request to load ssh keys.");
        try {
          SshKeyPair sshKeypair = karamelApiHandler.loadSshKeysIfExist();
          if (sshKeypair == null) {
            sshKeypair = karamelApiHandler.generateSshKeysAndUpdateConf();
          }
          karamelApiHandler.registerSshKeys(sshKeypair);
          response = Response.status(Response.Status.OK).entity(sshKeypair).build();
        } catch (KaramelException ex) {
          ex.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, ex.getMessage())).build();
        }

        return response;
      }

    }

    @Path("/registerSshKeys")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class Register {

      @PUT
      public Response registerSshKeys(SshKeyJSON sshKeysJSON) {
        Response response = null;
        Logger.getLogger(KaramelServiceApplication.class.getName()).
            log(Level.INFO, " Received request to register ssh keys.");
        SshKeyPair sshKeypair = new SshKeyPair();
        sshKeypair.setPublicKeyPath(sshKeysJSON.getPubKeyPath());
        sshKeypair.setPrivateKeyPath(sshKeysJSON.getPrivKeyPath());
        sshKeypair.setPassphrase(sshKeysJSON.getPassphrase());
        try {
          karamelApiHandler.registerSshKeys(sshKeypair);
          response = Response.status(Response.Status.OK).entity(sshKeypair).build();
        } catch (KaramelException ex) {
          ex.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, ex.getMessage())).build();
        }

        return response;
      }

    }

    @Path("/generateSshKeys")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class Generate {

      @PUT
      public Response generateSshKeys() {
        Response response = null;
        Logger.getLogger(KaramelServiceApplication.class.getName()).
            log(Level.INFO, " Received request to generate ssh keys.");
        try {
          SshKeyPair sshKeypair = karamelApiHandler.generateSshKeysAndUpdateConf();
          karamelApiHandler.registerSshKeys(sshKeypair);
          response = Response.status(Response.Status.OK).entity(sshKeypair).build();
        } catch (KaramelException ex) {
          ex.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, ex.getMessage())).build();
        }
        return response;
      }

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
        Logger.getLogger(KaramelServiceApplication.class.getName()).
            log(Level.INFO, " Received request to load the command cheatsheet.");
        try {
          String cheatSheet = karamelApiHandler.commandCheatSheet();
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

        Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.FINEST,
            " Received request to process a command with info: {0}", command.getCommand());
        try {
          CommandResponse cmdRes = karamelApiHandler.processCommand(command.getCommand(), command.getResult());
          command.setResult(cmdRes.getResult());
          command.setNextCmd(cmdRes.getNextCmd());
          command.setRenderer(cmdRes.getRenderer().name().toLowerCase());
          command.getMenuItems().addAll(cmdRes.getMenuItems());
          command.setSuccessmsg(cmdRes.getSuccessMessage());
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

    @Path("/loadCredentials")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class Load {

      @PUT
      public Response loadCredentials() {
        Response response = null;
        Logger.getLogger(KaramelServiceApplication.class.getName()).
            log(Level.INFO, " Received request to load the ec2 credentials.");
        try {
          Ec2Credentials credentials = karamelApiHandler.loadEc2CredentialsIfExist();
          ProviderJSON provider = new ProviderJSON();
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

    @Path("/validateCredentials")
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
      public Response validateCredentials(ProviderJSON providerJSON) {

        Response response = null;
        Logger.getLogger(KaramelServiceApplication.class.getName()).
            log(Level.INFO, " Received request to validate the ec2 credentials.");

        try {
          Ec2Credentials credentials = new Ec2Credentials();
          credentials.setAccessKey(providerJSON.getAccountId());
          credentials.setSecretKey(providerJSON.getAccountKey());
          if (karamelApiHandler.updateEc2CredentialsIfValid(credentials)) {
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

  /**
   * Place holder class dealing with separate cluster state handling.
   */
  public static class Cluster {

    @Path("/startCluster")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class StartCluster {

      @PUT
      public Response startCluster(KaramelBoardJSON boardJSON) {

        Response response = null;
        Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.INFO,
            "Start cluster: \n" + boardJSON.getJson());

        try {
          karamelApiHandler.startCluster(boardJSON.getJson());
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

  /**
   * Place holder class dealing with separate cluster state handling.
   */
  public static class Scaffolder {

    @Path("/scaffold")
    @Consumes(MediaType.APPLICATION_JSON)
    public static class ScaffoldCluster {

      @PUT
      public Response scaffold(ScaffoldJSON cbName) {
        Response response = null;
        Logger.getLogger(KaramelServiceApplication.class.getName()).
            log(Level.INFO, " Received request to scaffold a new cookbook.... ");
        try {
          CookbookScaffolder.create(cbName.getName());
          response = Response.status(Response.Status.OK).build();
        } catch (IOException e) {
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
            Thread.sleep(1000);
            System.exit(0);
          } catch (InterruptedException ex) {
            Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }.start();

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
        Logger.getLogger(KaramelServiceApplication.class.getName()).
            log(Level.INFO, " Received request to set sudo password.... ");
        try {
          karamelApiHandler.registerSudoPassword(sudoPwd.getPassword());
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

    @Path("/githubCredentials")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class GithubCredentials {

      @PUT
      public Response githubCredentials(GithubCredentialsJSON githubCreds) {
        Response response = null;
        Logger.getLogger(KaramelServiceApplication.class.getName()).
            log(Level.INFO, " Received request to set github credentials.... ");
        try {
          karamelApiHandler.registerGithubAccount(githubCreds.getEmail(), githubCreds.getPassword());
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
      Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.INFO, "Bye! Cleaning up first....");
      // TODO - interrupt all threads
      // Should we cleanup AMIs?
    }
  }

}
