package se.kth.karamel.backend.launcher.google;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jclouds.compute.RunNodesException;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.domain.Firewall;
import org.jclouds.googlecomputeengine.domain.Instance;
import org.jclouds.googlecomputeengine.domain.NewInstance;
import org.jclouds.googlecomputeengine.domain.Operation;
import org.jclouds.googlecomputeengine.features.InstanceApi;
import org.jclouds.googlecomputeengine.features.NetworkApi;
import org.jclouds.googlecomputeengine.features.OperationApi;
import org.jclouds.googlecomputeengine.options.FirewallOptions;
import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.client.model.Gce;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.InvalidCredentialsException;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author Hooman
 */
public class GceLauncher {

    private static final String GCE_PROVIDER = "gce";
    private static final Logger logger = Logger.getLogger(GceLauncher.class);
    public final GceContext context;

    public GceLauncher(GceContext context) {
        this.context = context;
    }

    /**
     *
     * @param confs
     * @return
     */
    public static Credentials readCredentials(Confs confs) {
        String jsonKeyFile = confs.getProperty(Settings.GCE_JSON_KEY_FILE_PATH);
        Credentials credentials = null;
        if (jsonKeyFile != null && !jsonKeyFile.isEmpty()) {
            try {
                String fileContents = Files.toString(new File(jsonKeyFile), Charset.defaultCharset());
                Supplier<Credentials> credentialSupplier = new GoogleCredentialsFromJson(fileContents);
                credentials = credentialSupplier.get();
            } catch (IOException ex) {
                logger.error("Error Reading the Json key file. Please check the provided path is correct.", ex);
            }
        }
        return credentials;
    }

    /**
     *
     * @param credentials
     * @return
     * @throws InvalidCredentialsException
     */
    public static GceContext validateCredentials(Credentials credentials) throws InvalidCredentialsException {
        try {
            GceContext context = new GceContext(credentials);
            GoogleComputeEngineApi gceApi = context.getGceApi();
            String projectName = gceApi.project().get().name();
            context.setProjectName(projectName);
            logger.info(String.format("Sucessfully Authenticated to project %s", projectName));
            return context;
        } catch (AuthorizationException e) {
            throw new InvalidCredentialsException("accountid:" + credentials.identity, e);
        }
    }

    // TODO: FireWalls in Google
    // TODO: Tags can be added.
    public void createFirewall(String clusterName, String groupName, String ipRange, Set<String> ports) throws KaramelException {
        String networkName = Settings.UNIQUE_GROUP_NAME(GCE_PROVIDER, clusterName, groupName);
        NetworkApi netApi = context.getNetworkApi();
        if (waitForOperation(context.getGceApi().operations(), netApi.createInIPv4Range(networkName, ipRange)) == 1) {
            throw new KaramelException("Failed to create network with name " + networkName);
        }
        URI networkUri = null;
        try {
            networkUri = Gce.buildNetworkUri(context.getProjectName(), networkName);
        } catch (URISyntaxException ex) {
            logger.error(ipRange);
            return;
        }
        List<Operation> operations = new ArrayList<>(ports.size());
        for (String port : ports) {
            String p;
            String pr;
            if (port.contains("/")) {
                String[] s = port.split("/");
                p = s[0];
                pr = s[1];
            } else {
                p = port;
                pr = "tcp";
            }
            FirewallOptions firewall = new FirewallOptions()
                    .addAllowedRule(Firewall.Rule.create(pr, ImmutableList.of(p)))
                    .addSourceRange("0.0.0.0/0");
            String fwName = Settings.UNIQUE_FIREWALL_NAME(networkName, p, pr);
            operations.add(context.getFireWallApi().createInNetwork(fwName, networkUri, firewall));
            logger.info(String.format("Ports became open for '%s'", networkName));
        }

        for (Operation op : operations) {
            // TODO: Handle failed operations and report them.
            waitForOperation(context.getGceApi().operations(), op);
        }
    }

    public List<MachineRuntime> forkMachines(String clusterName, String groupName, int totalSize, Gce gce) throws RunNodesException {
        List<MachineRuntime> machines = new ArrayList<>(totalSize);
        try {
            URI machineType = Gce.buildMachineTypeUri(context.getProjectName(), gce.getZone(), gce.getMachineType());
            URI networkType = Gce.buildDefaultNetworkUri(context.getProjectName());
            URI imageType = Gce.buildImageUri(gce.getImageType(), gce.getImageName());

            String uniqeGroupName = Settings.UNIQUE_GROUP_NAME(GCE_PROVIDER, clusterName, groupName);
            List<String> allVmNames = Settings.UNIQUE_VM_NAMES(GCE_PROVIDER, clusterName, groupName, totalSize);
            ArrayList<Operation> operations = new ArrayList<>(totalSize);
            logger.info(String.format("Start forking %d machine(s) for '%s' ...", totalSize, uniqeGroupName));
            InstanceApi instanceApi = context.getGceApi().instancesInZone(gce.getZone());
            for (String name : allVmNames) {
                Operation operation = instanceApi.create(NewInstance.create(name, machineType, networkType, imageType));
                logger.info("Starting instance " + name);
                operations.add(operation);
            }
            for (int i = 0; i < totalSize; i++) {
                if (waitForOperation(context.getGceApi().operations(), operations.get(i)) == 0) {
                    Instance vm = instanceApi.get(allVmNames.get(i));
                    MachineRuntime machine = new MachineRuntime(null); // TODO: add group run time.
                    machine.setEc2Id(vm.id());
                    machine.setName(vm.name());
                    Instance.NetworkInterface netInterface = vm.networkInterfaces().get(0);
                    machine.setPrivateIp(netInterface.networkIP());
                    machine.setPublicIp(netInterface.accessConfigs().get(0).natIP());
                    machines.add(machine);
                    //TODO: get instance's ssh port and user.
//                    machine.setSshPort(vm);
//                    machine.setSshUser(node.getCredentials().getUser());
                }
            }
        } catch (URISyntaxException ex) {
            logger.error("Wrong URI.", ex);
        }

        return machines;
    }

    public void cleanup(List<String> vmNames, String zone) throws KaramelException {
        logger.info(String.format("Killing following machines with names: \n %s.", vmNames.toString()));
        InstanceApi instanceApi = context.getGceApi().instancesInZone(zone);
        ArrayList<Operation> operations = new ArrayList<>(vmNames.size());
        for (String vm : vmNames) {
            operations.add(instanceApi.delete(vm));
        }
        for (int i = 0; i < operations.size(); i++) {
            if (waitForOperation(context.getGceApi().operations(), operations.get(i)) == 1) {
                logger.warn(String.format("Delete operations has timedout for vm %s.", vmNames.get(i)));
            } else {
                logger.info(String.format("Successfully deleted vm %s.", vmNames.get(i)));
            }
        }
    }

    private static int waitForOperation(OperationApi api, Operation operation) {
        // TODO: configurable timeout.
        //  TODO: write this method using org.jclouds.util.Predicates2.retry;
        int timeout = 60; // seconds
        int time = 0;

        while (operation.status() != Operation.Status.DONE) {
            if (time >= timeout) {
                return 1;
            }
            time++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn(e);
            }

            operation = api.get(operation.selfLink());
        }
        return 0;
    }
}
