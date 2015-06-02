package se.kth.karamel.backend.launcher.google;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecomputeengine.domain.Operation;
import org.jclouds.googlecomputeengine.features.OperationApi;
import org.junit.Test;
import se.kth.karamel.client.model.Gce;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.InvalidCredentialsException;

/**
 *
 * @author Hooman
 */
public class GceLauncherTest {

    public GceLauncherTest() {
    }

    /**
     * Test of readCredentials method, of class GceLauncher.
     */
    @Test
    public void testReadCredentials() {
        Confs confs = Confs.loadKaramelConfs();
        confs.put(Settings.GCE_JSON_KEY_FILE_PATH, Settings.KARAMEL_ROOT_PATH + File.separator + "gce-key.json");
        Credentials credentials = GceLauncher.readCredentials(confs);
        assert credentials != null;
        assert credentials.identity != null && !credentials.identity.isEmpty();
        assert credentials.credential != null && !credentials.credential.isEmpty();
    }

    /**
     * Test of validateCredentials method, of class GceLauncher.
     *
     * @throws se.kth.karamel.common.exception.InvalidCredentialsException
     */
    @Test
    public void validateCredentials() throws InvalidCredentialsException {
        Confs confs = Confs.loadKaramelConfs();
        confs.put(Settings.GCE_JSON_KEY_FILE_PATH, Settings.KARAMEL_ROOT_PATH + File.separator + "gce-key.json");
        Credentials credentials = GceLauncher.readCredentials(confs);
        GceContext context = GceLauncher.validateCredentials(credentials);
        assert context != null;
    }

    @Test
    public void testForkMachines() throws InvalidCredentialsException, RunNodesException, URISyntaxException {
        Confs confs = Confs.loadKaramelConfs();
        confs.put(Settings.GCE_JSON_KEY_FILE_PATH, Settings.KARAMEL_ROOT_PATH + File.separator + "gce-key.json");
        Credentials credentials = GceLauncher.readCredentials(confs);
        GceContext context = GceLauncher.validateCredentials(credentials);
        Gce gce = new Gce();
        context.setProjectName("fine-blueprint-95313");
        gce.setImageName("centos-6-v20150423");
        gce.setImageType(Gce.ImageType.centos);
        gce.setZone("europe-west1-b");
        gce.setMachineType(Gce.MachineType.n1_standard_1);
        GceLauncher launcher = new GceLauncher(context);
        launcher.forkMachines("c1", "g1", 10, gce);
    }
}
