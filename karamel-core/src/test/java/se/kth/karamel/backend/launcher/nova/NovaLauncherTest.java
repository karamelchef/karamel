package se.kth.karamel.backend.launcher.nova;

import com.google.common.base.Optional;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.NovaComputeService;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.rest.AuthorizationException;
import org.junit.Before;
import org.junit.Test;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.NovaCredentials;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;
import se.kth.karamel.common.settings.NovaSetting;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class NovaLauncherTest {

    private NovaContext novaContext;
    private SshKeyPair sshKeyPair;
    private NovaCredentials novaCredentials;
    private ContextBuilder builder;
    private ComputeServiceContext serviceContext;
    private NovaComputeService novaComputeService;
    private NovaApi novaApi;
    private SecurityGroupApi securityGroupApi;
    private KeyPairApi keyPairApi;
    private Optional<SecurityGroupApi> securityGroupApiOptional;
    private Optional<KeyPairApi> keyPairApiOptional;

    @Before
    public void setup(){

        //initialize value object
        novaCredentials = new NovaCredentials();
        novaCredentials.setAccountName("pepe");
        novaCredentials.setAccountPass("1234");
        novaCredentials.setEndpoint("http://sics-nova.se:8080");


        //Mocking external dependencies
        builder = mock(ContextBuilder.class);
        serviceContext = mock(ComputeServiceContext.class);
        novaComputeService = mock(NovaComputeService.class);
        securityGroupApi = mock(SecurityGroupApi.class);
        novaApi = mock(NovaApi.class);
        keyPairApi = mock(KeyPairApi.class);
        securityGroupApiOptional = mock(Optional.class);
        keyPairApiOptional = mock(Optional.class);

        when(builder.buildView(ComputeServiceContext.class)).thenReturn(serviceContext);
        when(serviceContext.getComputeService()).thenReturn(novaComputeService);

        when(novaComputeService.getContext()).thenReturn(serviceContext);
        when(serviceContext.unwrapApi(NovaApi.class)).thenReturn(novaApi);

        when(novaApi.getSecurityGroupApi(novaCredentials.getEndpoint())).thenReturn(securityGroupApiOptional);
        when(securityGroupApiOptional.get()).thenReturn(securityGroupApi);

        when(novaApi.getKeyPairApi(novaCredentials.getEndpoint())).thenReturn(keyPairApiOptional);
        when(keyPairApiOptional.get()).thenReturn(keyPairApi);
    }


    @Test
    public void validateNovaCredentialsTest() throws InvalidNovaCredentialsException {
        NovaContext context = NovaLauncher.validateCredentials(novaCredentials,builder);
        assertNotNull(context);
        assertNotNull(context.getSecurityGroupApi());
    }

    @Test(expected = InvalidNovaCredentialsException.class)
    public void validateNovaCredentialsTestException() throws InvalidNovaCredentialsException {
        when(securityGroupApi.list()).thenThrow(AuthorizationException.class);
        NovaContext context = NovaLauncher.validateCredentials(novaCredentials,builder);
    }

    @Test
    public void readNovaCredentialsTest() {
        Confs confs = mock(Confs.class);
        when(confs.getProperty(NovaSetting.NOVA_ACCOUNT_ID_KEY.getParameter())).thenReturn(novaCredentials.getAccountName());
        when(confs.getProperty(NovaSetting.NOVA_ACCESSKEY_KEY.getParameter())).thenReturn(novaCredentials.getAccountPass());
        when(confs.getProperty(NovaSetting.NOVA_ACCOUNT_ENDPOINT.getParameter())).thenReturn(novaCredentials.getEndpoint());
        NovaCredentials credentials = NovaLauncher.readCredentials(confs);
        assertNotNull(credentials);
    }

}