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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
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
import java.util.List;
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

/**
 * Created by babbarshaer on 2014-11-20.
 */
public class KaramelServiceApplication extends Application<KaramelServiceConfiguration> {

    private static KaramelApi karamelRestHandler;

    public static boolean launchBrowswer = true;

    public static TrayUI trayUi;

    private TemplateHealthCheck healthCheck;

    private static final Options options = new Options();
    private static final CommandLineParser parser = new GnuParser();

    static {
        options.addOption("y", false, "Do not prompt for user-supplied parameters. Accept default param values.");
        options.addOption("help", false, "Print help message.");
        options.addOption("nogui", false, "Run in a headless mode, without launching the Browser or SwingUI.");
        options.addOption(OptionBuilder.withArgName("launch")
                .hasArg()
                .withDescription("YAML file containing Karamel cluster definition")
                .create("yaml"));
    }

    public static void usage(int exitValue) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("karamel", options);
        System.exit(exitValue);
    }

    public static void main(String[] args) throws Exception {

        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "9191";
        }
        boolean cli = false;
        boolean launch = false;
        String yamlFile;

        List<String> csArgs = Arrays.asList(args);

        try {
            CommandLine line = parser.parse(options, args);
            if (line.getOptions().length > 0) {
                if (line.hasOption("help")) {
                    usage(0);
                } else if (line.hasOption("nogui")) {
                    launchBrowswer = false;
                    csArgs.remove("nogui");
                } else if (line.hasOption("launch")) {
                    cli = true;
                    int pos = csArgs.indexOf("launch");
                    csArgs.remove(pos + 1);
                    csArgs.remove("launch");
                }
            }
            if (cli) {
                yamlFile = line.getOptionValue("launch");
                // Try to open and compile the file. Print error msg if invalid file or invalid YAML.
            }
        } catch (ParseException e) {
            usage(-1);
        }

        karamelRestHandler = new KaramelApiImpl();
        if (args.length > 0 && args[0].compareToIgnoreCase("nolaunch") == 0) {
            launchBrowswer = false;
            args[0] = args[1];
            args[1] = args[2];
        }
        if (cli) {
            KaramelApi k = new KaramelApiImpl();
            // TODO - KaramelApi needs to support taking a File a a parameter
        } else {
            new KaramelServiceApplication().run(args);
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
        FilterRegistration

.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class  

    );

        // Allow cross origin requests.
    filter.addMappingForUrlPatterns (EnumSet.allOf

    (DispatcherType.class  

        ), true, "/*");
        filter.setInitParameter (

        "allowedOrigins", "*"); // allowed origins comma separated
        filter.setInitParameter (

        "allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filter.setInitParameter (

        "allowedMethods", "GET,PUT,POST,DELETE,OPTIONS,HEAD");
        filter.setInitParameter (

        "preflightMaxAge", "5184000"); // 2 months
        filter.setInitParameter (

        "allowCredentials", "true");

        environment.jersey ()

        .setUrlPattern("/api/*");

        environment.healthChecks ()

        .register("template", healthCheck);

        environment.jersey ()

        .register(new ConvertYamlToJSON());
        environment.jersey ()

        .register(new ConvertJSONToYaml());
        environment.jersey ()

        .register(new Cookbook());
        environment.jersey ()

        .register(new ProviderValidation());
        environment.jersey ()

        .register(new Cluster.StartCluster());
        environment.jersey ()
        .register(new Cluster.ViewCluster());

        if (launchBrowswer

        
            ) {
            openWebpage(new URL("http://localhost:" + getPort(environment) + "/index.html"));

            if (SystemTray.isSupported()) {
                trayUi = new TrayUI(createImage("if.png", "tray icon"), getPort(environment));
            }

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
     * For the cluster yml supplied by the UI, convert it into JSON Object and
     * return.
     */
    @Path("/fetchJson")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class ConvertYamlToJSON {

        @PUT
        public Response getJSONForYaml(KaramelBoardYaml cluster) {

            Response response = null;
            try {
                String jsonClusterString = karamelRestHandler.yamlToJson(cluster.getYml());
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
                String yml = karamelRestHandler.jsonToYaml(karamelBoardJSON.getJson());
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
                String cookbookDetails = karamelRestHandler.getCookbookDetails(cookbookJSON.getUrl(), cookbookJSON.isRefresh());
                response = Response.status(Response.Status.OK).entity(cookbookDetails).build();

            } catch (KaramelException e) {
                e.printStackTrace();
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
            }

            return response;
        }
    }

    @Path("/validateProvider")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class ProviderValidation {

        /**
         * Validating the Provider based on the supplied credentials..
         *
         * @param providerJSON
         * @return
         */
        @PUT
        public Response validateProvider(ProviderJSON providerJSON) {

            Response response = null;
            System.out.println(" Received request to validate the ec2 account.");

            try {
                if (karamelRestHandler.updateEc2CredentialsIfValid(providerJSON.getAccountId(), providerJSON.getAccountKey())) {
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
                    karamelRestHandler.startCluster(boardJSON.getJson());
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

                    String clusterInfo = karamelRestHandler.getClusterStatus(clusterJSON.getClusterName());
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
