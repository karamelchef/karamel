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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.Properties;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
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
import org.eclipse.jetty.servlets.CrossOriginFilter;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.ClusterManager;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.common.CookbookScaffolder;
import static se.kth.karamel.common.CookbookScaffolder.deleteRecursive;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.webservice.calls.cluster.ProcessCommand;
import se.kth.karamel.webservice.calls.cluster.StartCluster;
import se.kth.karamel.webservice.calls.definition.FetchCookbook;
import se.kth.karamel.webservice.calls.definition.JsonToYaml;
import se.kth.karamel.webservice.calls.definition.YamlToJson;
import se.kth.karamel.webservice.calls.ec2.LoadEc2Credentials;
import se.kth.karamel.webservice.calls.ec2.ValidateEc2Credentials;
import se.kth.karamel.webservice.calls.gce.LoadGceCredentials;
import se.kth.karamel.webservice.calls.gce.ValidateGceCredentials;
import se.kth.karamel.webservice.calls.nova.LoadNovaCredentials;
import se.kth.karamel.webservice.calls.nova.ValidateNovaCredentials;
import se.kth.karamel.webservice.calls.occi.LoadOcciCredentials;
import se.kth.karamel.webservice.calls.occi.ValidateOcciCredentials;
import se.kth.karamel.webservice.calls.sshkeys.GenerateSshKeys;
import se.kth.karamel.webservice.calls.sshkeys.LoadSshKeys;
import se.kth.karamel.webservice.calls.sshkeys.RegisterSshKeys;
import se.kth.karamel.webservice.calls.sshkeys.SetSudoPassword;
import se.kth.karamel.webservice.calls.system.ExitKaramel;
import se.kth.karamel.webservice.calls.system.PingServer;
import se.kth.karamel.webservice.utils.TemplateHealthCheck;

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
  private static boolean cli = false;
  private static boolean headless = false;
  private static boolean noSudoPasswd = false;

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
    options.addOption("headless", false, "Launch Karamel from a headless server (no terminal on the server).");
//    options.addOption("passwd", false, "Sudo password");
    options.addOption(OptionBuilder.withArgName("sudoPassword")
      .hasArg()
      .withDescription("Sudo password")
      .create("passwd"));
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

  private String getKaramelVersion() {
    final Properties props = new Properties();
    try {
      props.load(getClass().getClassLoader().getResourceAsStream("karamel.properties"));
    } catch (IOException ex) {
      return "unknown";
    }
    return props.getProperty("karamel.version", "unknown");
  }

  public static void main(String[] args) throws Exception {

    System.setProperty("java.net.preferIPv4Stack", "true");
    String yamlTxt;

    // These args are sent to the Dropwizard app (thread)
    String[] modifiedArgs = new String[2];
    modifiedArgs[0] = "server";
    String sudoPasswd = "";

    karamelApi = new KaramelApiImpl();
    KaramelServiceApplication karamelServiceApplication = new KaramelServiceApplication();
    System.out.println("Version: " + karamelServiceApplication.getKaramelVersion());

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
        headless = true;
      }
      if (line.hasOption("headless")) {
        headless = true;
      }
      if (line.hasOption("passwd")) {
        sudoPasswd = line.getOptionValue("passwd");        
      } else {
        noSudoPasswd = true;
      }

      if (cli) {

        ClusterManager.EXIT_ON_COMPLETION  = true;
//        if (!noSudoPasswd) {
//          Console c = null;
//          c = System.console();
//          if (c == null) {
//            System.err.println("No console available.");
//            System.exit(1);
//          }
//          sudoPasswd = c.readLine("Enter your sudo password (just press 'enter' if you don't have one):");
//        }
        karamelServiceApplication.run(modifiedArgs);
        logger.info(String.format("Version: %s", karamelServiceApplication.getKaramelVersion()));

        Thread.currentThread().sleep(2000);

        // Try to open and read the yaml file. 
        // Print error msg if invalid file or invalid YAML.
        yamlTxt = CookbookScaffolder.readFile(line.getOptionValue("launch"));
        YamlCluster cluster = ClusterDefinitionService.yamlToYamlObject(yamlTxt);
        String jsonTxt = karamelApi.yamlToJson(yamlTxt);

        if (!noSudoPasswd && sudoPasswd.isEmpty() == false) {
          karamelApi.registerSudoPassword(sudoPasswd);
        }

        SshKeyPair pair = karamelApi.loadSshKeysIfExist();

        karamelApi.registerSshKeys(pair);

        karamelApi.startCluster(jsonTxt);

        long ms1 = System.currentTimeMillis();
        while (ms1 + 60000000 > System.currentTimeMillis()) {
//          String clusterStatus = karamelApi.getClusterStatus(cluster.getName());
//          logger.debug(clusterStatus);

          Thread.currentThread().sleep(30000);
        }
      }
    } catch (ParseException e) {
      usage(-1);
    } catch (KaramelException e) {
      System.err.println("Inalid yaml file; " + e.getMessage());
      System.exit(-2);
    }

    if (!cli) {
      karamelServiceApplication.run(modifiedArgs);
      logger.info(String.format("Version: %s", karamelServiceApplication.getKaramelVersion()));
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

    //definitions
    environment.jersey().register(new YamlToJson(karamelApi));
    environment.jersey().register(new JsonToYaml(karamelApi));
    environment.jersey().register(new FetchCookbook(karamelApi));

    //ssh
    environment.jersey().register(new LoadSshKeys(karamelApi));
    environment.jersey().register(new RegisterSshKeys(karamelApi));
    environment.jersey().register(new GenerateSshKeys(karamelApi));
    environment.jersey().register(new SetSudoPassword(karamelApi));

    //ec2
    environment.jersey().register(new LoadEc2Credentials(karamelApi));
    environment.jersey().register(new ValidateEc2Credentials(karamelApi));

    //gce
    environment.jersey().register(new LoadGceCredentials(karamelApi));
    environment.jersey().register(new ValidateGceCredentials(karamelApi));

    //cluster
    environment.jersey().register(new StartCluster(karamelApi));
    environment.jersey().register(new ProcessCommand(karamelApi));

    environment.jersey().register(new ExitKaramel(karamelApi));
    environment.jersey().register(new PingServer(karamelApi));

    //Openstack nova
    environment.jersey().register(new LoadNovaCredentials(karamelApi));
    environment.jersey().register(new ValidateNovaCredentials(karamelApi));

    //occi
    environment.jersey().register(new LoadOcciCredentials(karamelApi));
    environment.jersey().register(new ValidateOcciCredentials(karamelApi));

    // Wait to make sure jersey/angularJS is running before launching the browser
    final int webPort = getPort(environment);

    if (!headless) {
      if (SystemTray.isSupported()) {
        trayUi = new TrayUI(createImage("if.png", "tray icon"), getPort(environment));
      }

      new Thread("webpage opening..") {
        public void run() {
          try {
            Thread.sleep(1500);
            openWebpage(new URL("http://localhost:" + webPort + "/index.html#/"));
          } catch (InterruptedException e) {
            // swallow the exception
          } catch (java.net.MalformedURLException e) {
            // swallow the exception
          }
        }
      }.start();

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

  static class KaramelCleanupBeforeShutdownThread extends Thread {

    @Override
    public void run() {
      logger.info("Bye! Cleaning up first....");
      // TODO - interrupt all threads
      // Should we cleanup AMIs?
    }
  }

}
