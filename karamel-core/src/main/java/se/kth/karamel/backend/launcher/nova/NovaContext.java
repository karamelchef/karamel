package se.kth.karamel.backend.launcher.nova;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.NovaComputeService;
import org.jclouds.openstack.nova.v2_0.compute.extensions.NovaSecurityGroupExtension;
import org.jclouds.openstack.nova.v2_0.config.NovaProperties;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.sshj.config.SshjSshClientModule;
import se.kth.karamel.common.NovaCredentials;
import org.jclouds.compute.ComputeService;

import java.util.Properties;

/**
 * Created by Alberto on 2015-05-16.
 */
public class NovaContext {

    private final NovaCredentials novaCredentials;
    private final NovaComputeService computeService;
    private final NovaApi novaApi;
    private final SecurityGroupApi securityGroupApi;
    private final KeyPairApi keyPairApi;

    public NovaContext(NovaCredentials credentials){
        this.novaCredentials = credentials;
        Properties properties = new Properties();
        Iterable<Module> modules = ImmutableSet.<Module>of(
                new SshjSshClientModule(),
                new SLF4JLoggingModule(),
                new EnterpriseConfigurationModule());

        ContextBuilder build = ContextBuilder.newBuilder("aws-ec2")
                .credentials(credentials.getAccountName(), credentials.getAccountPass())
                .endpoint(credentials.getEndpoint())
                .modules(modules)
                .overrides(properties);

        ComputeServiceContext context = build.buildView(ComputeServiceContext.class);
        this.computeService = (NovaComputeService) context.getComputeService();
        this.novaApi = computeService.getContext().unwrapApi(NovaApi.class);
        this.securityGroupApi = novaApi.getSecurityGroupApi(credentials.getEndpoint()).get();
        this.keyPairApi = (KeyPairApi) novaApi.getKeyPairApi(credentials.getEndpoint()).get();
    }

}
