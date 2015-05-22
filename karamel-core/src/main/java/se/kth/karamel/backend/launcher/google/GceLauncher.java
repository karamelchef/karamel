package se.kth.karamel.backend.launcher.google;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.SecurityGroup;
import org.jclouds.compute.extensions.SecurityGroupExtension;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.InvalidCredentialsException;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author Hooman
 */
public class GceLauncher {

    private static final Logger logger = Logger.getLogger(GceLauncher.class);
    ComputeServiceContext context;

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
            logger.info(String.format("Sucessfully Authenticated to project %s\n", projectName));
            return context;
        } catch (AuthorizationException e) {
            throw new InvalidCredentialsException("accountid:" + credentials.identity, e);
        }
    }

    // FireWalls in Google
    public String createSecurityGroup(String clusterName, String groupName, Location location, Set<String> ports) throws KaramelException {
        String uniqeGroupName = Settings.GCE_UNIQUE_GROUP_NAME(clusterName, groupName);
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
}
