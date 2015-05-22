package se.kth.karamel.backend.launcher.google;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.features.FirewallApi;

/**
 *
 * @author Hooman
 */
public class GceContext {

    Credentials credentials;
    private final ComputeService computeService;
    private final GoogleComputeEngineApi gceApi;

    public GceContext(Credentials credentials) {
        ComputeServiceContext context = ContextBuilder.newBuilder("google-compute-engine")
                .credentials(credentials.identity, credentials.credential)
                .buildView(ComputeServiceContext.class);
        computeService = context.getComputeService();
        gceApi = context.unwrapApi(GoogleComputeEngineApi.class);

    }

    public Credentials getCredentials() {
        return credentials;
    }

    public ComputeService getComputeService() {
        return computeService;
    }

    public GoogleComputeEngineApi getGceApi() {
        return gceApi;
    }
}
