package se.kth.karamel.backend.launcher.google;

import java.io.File;
import org.jclouds.domain.Credentials;
import org.junit.Test;
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

}
