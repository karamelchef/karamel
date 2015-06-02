package se.kth.karamel.backend.launcher.google;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
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
import org.jclouds.compute.domain.SecurityGroup;
import org.jclouds.compute.extensions.SecurityGroupExtension;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.domain.NewInstance;
import org.jclouds.googlecomputeengine.domain.Operation;
import org.jclouds.googlecomputeengine.features.InstanceApi;
import org.jclouds.googlecomputeengine.features.OperationApi;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;
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
            logger.info(String.format("Sucessfully Authenticated to project %s", projectName));
            return context;
        } catch (AuthorizationException e) {
            throw new InvalidCredentialsException("accountid:" + credentials.identity, e);
        }
    }

    // FireWalls in Google
    public String createSecurityGroup(String clusterName, String groupName, Location location, Set<String> ports) throws KaramelException {
        String uniqeGroupName = Settings.UNIQUE_GROUP_NAME(GCE_PROVIDER, clusterName, groupName);
        logger.info(String.format("Creating security group '%s' ...", uniqeGroupName));

        Optional<SecurityGroupExtension> securityGroupExt
                = context.getComputeService().getSecurityGroupExtension();
        if (securityGroupExt.isPresent()) {
            SecurityGroupExtension extension = securityGroupExt.get();
            SecurityGroup securityGroup = extension.createSecurityGroup(uniqeGroupName, location);
            String groupId = securityGroup.getId();
            for (String port : ports) {
                Integer p;
                IpProtocol pr;
                if (port.contains("/")) {
                    String[] s = port.split("/");
                    p = Integer.valueOf(s[0]);
                    pr = IpProtocol.valueOf(s[1]);
                } else {
                    p = Integer.valueOf(port);
                    pr = IpProtocol.TCP;
                }
                IpPermission permission = new IpPermission(pr, p, p, null, ImmutableSet.<String>of(groupId), ImmutableSet.<String>of("0.0.0.0/0"), null);
                extension.addIpPermission(permission, securityGroup);
                logger.info(String.format("Ports became open for '%s'", uniqeGroupName));
            }
            logger.info(String.format("Security group '%s' was created :)", uniqeGroupName));
            return groupId;
        }
        return null;
    }

    public List<MachineRuntime> forkMachines(String clusterName, String groupName, int totalSize, Gce gce) throws RunNodesException {
        try {
            String uniqeGroupName = Settings.UNIQUE_GROUP_NAME(GCE_PROVIDER, clusterName, groupName);
            List<String> allVmNames = Settings.UNIQUE_VM_NAMES(GCE_PROVIDER, clusterName, groupName, totalSize);
            logger.info(String.format("Start forking %d machine(s) for '%s' ...", totalSize, uniqeGroupName));
            InstanceApi instanceApi = context.getGceApi().instancesInZone(gce.getZone());
            URI machineType = Gce.buildMachineTypeUri(context.getProjectName(), gce.getZone(), gce.getMachineType());
            URI networkType = Gce.buildNetworkUri(context.getProjectName());
            URI imageType = Gce.buildImageUri(gce.getImageType(), gce.getImageName());
            ArrayList<Operation> operations = new ArrayList<Operation>();
            for (String name : allVmNames) {
                Operation operation = instanceApi.create(NewInstance.create(allVmNames.get(0), machineType, networkType, imageType));
                operations.add(operation);
            }
            ArrayList<URI> instances = new ArrayList<URI>();
            for (Operation op : operations) {
                waitForOperation(context.getGceApi().operations(), op);
                instances.add(op.targetLink());
            }
        } catch (URISyntaxException ex) {
            logger.error("Wrong URI.", ex);
        }

        List<MachineRuntime> machines = new ArrayList<>(totalSize);
        // TODO: add to the machines list using target links..

        return machines;
    }

    private static int waitForOperation(OperationApi api, Operation operation) {
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
                e.printStackTrace();
            }

            operation = api.get(operation.selfLink());
        }
        //TODO: Check for errors.
        return 0;
    }
}
