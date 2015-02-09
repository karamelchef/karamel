package se.kth.karamel.webservice;

import icons.TrayUI;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.SystemTray;
import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservicemodel.*;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
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
import se.kth.karamel.client.model.yaml.YamlCluster;
import se.kth.karamel.client.model.yaml.YamlUtil;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.SshKeysNotfoundException;

/**
 * Created by babbarshaer on 2014-11-20.
 */
public class KaramelServiceApplication extends Application<KaramelServiceConfiguration> {

  private static KaramelApi karamelApiHandler;

  public static TrayUI trayUi;

  private TemplateHealthCheck healthCheck;

  private static final Options options = new Options();
  private static final CommandLineParser parser = new GnuParser();

  static {
//        options.addOption("y", false, "Do not prompt for user-supplied parameters. Accept default param values.");
    options.addOption("help", false, "Print help message.");
    options.addOption(OptionBuilder.withArgName("yamlFile")
            .hasArg()
            .withDescription("Dropwizard configuration in a YAML file")
            .create("server"));
    options.addOption(OptionBuilder.withArgName("yamlFile")
            .hasArg()
            .withDescription("Karamel cluster definition in a YAML file")
            .create("launch"));
  }

  public static void usage(int exitValue) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("karamel", options);
    System.exit(exitValue);
  }

  static String readFile(String path)
          throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded);
  }

  public static void main(String[] args) throws Exception {

    String webPort = System.getenv("PORT");
    if (webPort == null || webPort.isEmpty()) {
      webPort = "9191";
    }
    boolean cli = false;
    boolean launch = false;
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
          yamlTxt = readFile(line.getOptionValue("launch"));
          YamlCluster cluster = YamlUtil.loadCluster(yamlTxt);
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
              ec2AccountId = c.readLine("Enter your Ec2 Account-Id:");
            }
            if (ec2AccessKey == null || ec2AccessKey.isEmpty()) {
              char[] secretKeyChars = c.readPassword("Enter your Ec2 Access-Key:");
              ec2AccessKey = new String(secretKeyChars);
            }
            credentials = new Ec2Credentials();
            credentials.setAccountId(ec2AccountId);
            credentials.setAccessKey(ec2AccessKey);
            valid = karamelApiHandler.updateEc2CredentialsIfValid(credentials);
            if (!valid) {
              System.out.println("Invalid Ec2 Credentials. Try again.");
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
  }

// Name of the application displayed when application boots up.
  @Override
  public String getName() {
    return "caramel-core";
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
    environment.jersey().register(new Ssh.Generate());
    environment.jersey().register(new Ec2.Load());
    environment.jersey().register(new Ec2.Validate());
    environment.jersey().register(new Cluster.StartCluster());
    environment.jersey().register(new Cluster.ViewCluster());
    environment.jersey().register(new Command.CheatSheet());
    environment.jersey().register(new Command.Process());
    
    // Wait to make sure jersey/angularJS is running before launching the browser
    Thread.sleep(300);
    openWebpage(new URL("http://localhost:" + getPort(environment) + "/index.html"));

    if (SystemTray.isSupported()) {
      trayUi = new TrayUI(createImage("if.png", "tray icon"), getPort(environment));
    }

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
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
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
      System.out.println("Fetch Yaml Called ... ");

      try {
        String yml = karamelApiHandler.jsonToYaml(karamelBoardJSON.getJson());
        KaramelBoardYaml karamelBoardYaml = new KaramelBoardYaml(yml);
        response = Response.status(Response.Status.OK).entity(karamelBoardYaml).build();

      } catch (KaramelException e) {
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
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

      System.out.println("Received Call For the cookbook.");
      System.out.println(cookbookJSON.getUrl());
      try {
        String cookbookDetails = karamelApiHandler.getCookbookDetails(cookbookJSON.getUrl(), cookbookJSON.isRefresh());
        response = Response.status(Response.Status.OK).entity(cookbookDetails).build();

      } catch (KaramelException e) {
        e.printStackTrace();
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
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
        System.out.println(" Received request to load ssh keys.");
        SshKeyPair sshKeypair = null;
        try {
          sshKeypair = karamelApiHandler.loadSshKeysIfExist();
          if (sshKeypair == null) {
            sshKeypair = karamelApiHandler.generateSshKeysAndUpdateConf();
          }
          karamelApiHandler.registerSshKeys(sshKeypair);
          response = Response.status(Response.Status.OK).entity(sshKeypair).build();
        } catch (KaramelException ex) {
          ex.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, ex.getMessage())).build();
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
        System.out.println(" Received request to generate ssh keys.");
        try {
          SshKeyPair sshKeypair = karamelApiHandler.generateSshKeysAndUpdateConf();
          karamelApiHandler.registerSshKeys(sshKeypair);
          response = Response.status(Response.Status.OK).entity(sshKeypair).build();
        } catch (KaramelException ex) {
          ex.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, ex.getMessage())).build();
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
        System.out.println(" Received request to load the command cheatsheet.");
        try {
          String cheatSheet = karamelApiHandler.commandCheatSheet();
          response = Response.status(Response.Status.OK).entity(cheatSheet).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
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
        System.out.println(" Received request to process a command with info: " + command.getCommand());
        try {
          String cheatSheet = karamelApiHandler.processCommand(command.getCommand());
          System.out.println(" Cheat Sheet Information: " + cheatSheet);
            
          response = Response.status(Response.Status.OK).entity(command).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
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
        System.out.println(" Received request to load the ec2 credentials.");
        try {
          Ec2Credentials credentials = karamelApiHandler.loadEc2CredentialsIfExist();
          ProviderJSON provider = new ProviderJSON();
          provider.setAccountId((credentials == null) ? "" : credentials.getAccountId());
          provider.setAccountKey((credentials == null) ? "" : credentials.getAccessKey());
          response = Response.status(Response.Status.OK).entity(provider).build();
        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
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
        System.out.println(" Received request to validate the ec2 credentials.");

        try {
          Ec2Credentials credentials = new Ec2Credentials();
          credentials.setAccountId(providerJSON.getAccountId());
          credentials.setAccessKey(providerJSON.getAccountKey());
          if (karamelApiHandler.updateEc2CredentialsIfValid(credentials)) {
            response = Response.status(Response.Status.OK).entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
          } else {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, "Invalid Credentials")).build();
          }

        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
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
        System.out.println(" Received request to start the cluster. ");

        try {
          karamelApiHandler.startCluster(boardJSON.getJson());
          response = Response.status(Response.Status.OK).entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();

        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }

        return response;
      }
    }

    @Path("/viewCluster")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class ViewCluster {

      @PUT
      public Response viewCluster(ClusterJSON clusterJSON) {

        Response response = null;
        System.out.println(" Received request to view the cluster.... ");

        try {

          String clusterInfo = karamelApiHandler.getClusterStatus(clusterJSON.getClusterName());
          response = Response.status(Response.Status.OK).entity(clusterInfo).build();

        } catch (KaramelException e) {
          e.printStackTrace();
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
        }

        return response;
      }
    }

  }

}
