package se.kth.karamel.backend.launcher.nova;

import com.google.common.base.Optional;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.NovaComputeService;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.junit.Before;
import org.junit.Test;
import se.kth.karamel.common.util.NovaCredentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NovaContextTest {
    private NovaCredentials credentials;
    private ContextBuilder builder;
    private ComputeServiceContext serviceContext;
    private NovaComputeService novaComputeService;
    private NovaApi novaApi;
    private SecurityGroupApi securityGroupApi;
    private KeyPairApi keyPairApi;

    @Before
    public void setup(){
        credentials = mock(NovaCredentials.class);
        builder = mock(ContextBuilder.class);
        serviceContext = mock(ComputeServiceContext.class);
        novaComputeService = mock(NovaComputeService.class);
        securityGroupApi = mock(SecurityGroupApi.class);
        novaApi = mock(NovaApi.class);
        keyPairApi = mock(KeyPairApi.class);

        ComputeServiceContext context = mock(ComputeServiceContext.class);
        Optional<SecurityGroupApi> securityGroupApiOptional = mock(Optional.class);
        Optional<KeyPairApi> keyPairApiOptional = mock(Optional.class);

        when(credentials.getAccountName()).thenReturn("pepe");
        when(credentials.getAccountPass()).thenReturn("1234");
        when(credentials.getEndpoint()).thenReturn("nova endpoint");
        when(credentials.getRegion()).thenReturn("region");

        when(builder.credentials(credentials.getAccountName(), credentials.getAccountPass())).thenReturn(builder);
        when(builder.endpoint(credentials.getEndpoint())).thenReturn(builder);
        when(builder.buildView(ComputeServiceContext.class)).thenReturn(serviceContext);
        when(serviceContext.getComputeService()).thenReturn(novaComputeService);

        when(novaComputeService.getContext()).thenReturn(context);
        when(context.unwrapApi(NovaApi.class)).thenReturn(novaApi);

        when(novaApi.getSecurityGroupApi(credentials.getRegion())).thenReturn(securityGroupApiOptional);
        when(securityGroupApiOptional.get()).thenReturn(securityGroupApi);

        when(novaApi.getKeyPairApi(credentials.getRegion())).thenReturn(keyPairApiOptional);
        when(keyPairApiOptional.get()).thenReturn(keyPairApi);

    }

    @Test
    public void generateContextBuilder(){
        ContextBuilder contextBuilder = NovaContext.buildContext(credentials);
        assertNotNull(contextBuilder);
    }

    @Test
    public void sanityCheck(){
        NovaContext novaContext = new NovaContext(credentials,builder);
        assertNotNull(novaContext.getComputeService());
        assertNotNull(novaContext.getNovaApi());
        assertNotNull(novaContext.getNovaCredentials());
        assertNotNull(novaContext.getKeyPairApi());
        assertNotNull(novaContext.getSecurityGroupApi());

        assertEquals("pepe",credentials.getAccountName());
        assertEquals("1234",credentials.getAccountPass());
        assertEquals("nova endpoint", credentials.getEndpoint());

    }

}