package se.kth.karamel.backend.launcher.nova;

import com.google.common.base.Optional;
import com.google.inject.matcher.Matchers;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.NovaComputeService;
import org.jclouds.openstack.nova.v2_0.domain.Ingress;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroupRule;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.rest.AuthorizationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Any;
import se.kth.karamel.client.model.Nova;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.NovaCredentials;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.settings.NovaSetting;

import java.util.HashSet;
import java.util.Set;

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
    private Optional securityGroupApiOptional;
    private Optional keyPairApiOptional;
    private SecurityGroup securityGroupCreated;

    private String clusterName;
    private String groupName;
    private Nova nova;
    private static final Set<String> ports= new HashSet<>();

    static{
        for(int i=100;i<=250;i++){
            ports.add(i+"");
        }
    }

    @Before
    public void setup(){

        //initialize value objects
        novaCredentials = new NovaCredentials();
        novaCredentials.setAccountName("pepe");
        novaCredentials.setAccountPass("1234");
        novaCredentials.setEndpoint("http://sics-nova.se:8080");
        novaCredentials.setRegion("SICSRegion");


        nova = new Nova();
        nova.setRegion("SICSRegion");
        nova.setEndpoint(novaCredentials.getEndpoint());
        nova.setImage("ubuntu14.04");
        nova.setFlavor("1");

        sshKeyPair = new SshKeyPair();
        sshKeyPair.setPrivateKey("this is my private key");
        sshKeyPair.setPublicKey("this is my public key");
        sshKeyPair.setPassphrase("helloworld");
        sshKeyPair.setPrivateKeyPath("/pathToPrivateKey");
        sshKeyPair.setPublicKeyPath("/pathToPublicKey");

        clusterName = "novaTest";
        groupName = "dummyGroup";

        //Mocking external dependencies
        builder = mock(ContextBuilder.class);
        serviceContext = mock(ComputeServiceContext.class);
        novaComputeService = mock(NovaComputeService.class);
        securityGroupApi = mock(SecurityGroupApi.class);
        novaApi = mock(NovaApi.class);
        keyPairApi = mock(KeyPairApi.class);
        securityGroupApiOptional = mock(Optional.class);
        keyPairApiOptional = mock(Optional.class);
        novaContext = mock(NovaContext.class);
        securityGroupCreated = mock(SecurityGroup.class);

        when(novaContext.getNovaCredentials()).thenReturn(novaCredentials);
        when(novaContext.getNovaApi()).thenReturn(novaApi);

        when(builder.buildView(ComputeServiceContext.class)).thenReturn(serviceContext);
        when(serviceContext.getComputeService()).thenReturn(novaComputeService);

        when(novaComputeService.getContext()).thenReturn(serviceContext);
        when(serviceContext.unwrapApi(NovaApi.class)).thenReturn(novaApi);

        when(novaApi.getSecurityGroupApi(nova.getRegion())).thenReturn(securityGroupApiOptional);
        when(securityGroupApiOptional.isPresent()).thenReturn(true);
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
        when(confs.getProperty(NovaSetting.NOVA_REGION.getParameter())).thenReturn(novaCredentials.getRegion());
        NovaCredentials credentials = NovaLauncher.readCredentials(confs);
        assertNotNull(credentials);
    }

    @Test(expected = KaramelException.class)
    public void createNovaLauncherTestNullContext() throws KaramelException{
        NovaLauncher launcher = new NovaLauncher(null,sshKeyPair);
    }

    @Test(expected = KaramelException.class)
    public void createNovaLauncherTestNullSSHKey() throws KaramelException{
        NovaLauncher launcher = new NovaLauncher(novaContext,null);
    }

    @Test
    public void createSecurityGroupTestWithTestingFlag()throws KaramelException{
        //Initializing and mocking need for method test
        SecurityGroupRule rule = mock(SecurityGroupRule.class);
        String uniqueGroup = NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName,groupName);
        String uniqueDescription = NovaSetting.NOVA_UNIQUE_GROUP_DESCRIPTION(clusterName,groupName);

        Ingress ingress = Ingress.builder()
                .fromPort(0)
                .toPort(65535)
                .ipProtocol(IpProtocol.TCP)
                .build();

        when(securityGroupApi.createWithDescription(uniqueGroup,uniqueDescription)).thenReturn(securityGroupCreated);
        when(securityGroupCreated.getId()).thenReturn("10");
        when(securityGroupApi.createRuleAllowingCidrBlock("10", ingress, "0.0.0.0/0")).thenReturn(rule);

        NovaLauncher novaLauncher = new NovaLauncher(novaContext,sshKeyPair);
        String groupId = novaLauncher.createSecurityGroup(clusterName,groupName,nova,ports);
        assertEquals("10",groupId);
    }

    @Test
    public void createSecurityGroupTestWithTestingFlagAndNoSecurityAPIPresent()throws KaramelException{
        //Initializing and mocking need for method test
        String uniqueGroup = NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName,groupName);
        String uniqueDescription = NovaSetting.NOVA_UNIQUE_GROUP_DESCRIPTION(clusterName,groupName);

        when(securityGroupApiOptional.isPresent()).thenReturn(false);

        NovaLauncher novaLauncher = new NovaLauncher(novaContext,sshKeyPair);
        String groupId = novaLauncher.createSecurityGroup(clusterName,groupName,nova,ports);
        assertNull(groupId);
    }

}