package se.kth.karamel.backend.launcher.nova;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.NovaComputeService;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.Ingress;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroupRule;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.rest.AuthorizationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.NodeRunTime;
import se.kth.karamel.common.clusterdef.Nova;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.util.NovaCredentials;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.util.settings.NovaSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NovaLauncherTest {

  private static final Set<String> ports = new HashSet<>();

  static {
    for (int i = 100; i <= 250; i++) {
      ports.add(i + "");
    }
  }

  private NovaContext novaContext;
  private SshKeyPair sshKeyPair;
  private NovaCredentials novaCredentials;
  private ContextBuilder builder;
  private ComputeServiceContext serviceContext;
  private ComputeService novaComputeService;
  private NovaApi novaApi;
  private SecurityGroupApi securityGroupApi;
  private KeyPairApi keyPairApi;
  private Optional securityGroupApiOptional;
  private Optional keyPairApiOptional;
  private SecurityGroup securityGroupCreated;
  private String clusterName;
  private String groupName;
  private Nova nova;

  @Before
  public void setup() {

    //initialize value objects
    novaCredentials = new NovaCredentials();
    novaCredentials.setAccountName("pepe");
    novaCredentials.setAccountPass("1234");
    novaCredentials.setEndpoint("http://sics-nova.se:8080");
    novaCredentials.setRegion("SICSRegion");


    nova = new Nova();
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
    when(novaContext.getKeyPairApi()).thenReturn(keyPairApi);
    when(novaContext.getComputeService()).thenReturn(novaComputeService);

    when(builder.credentials(novaCredentials.getAccountName(),novaCredentials.getAccountPass())).thenReturn(builder);
    when(builder.endpoint(novaCredentials.getEndpoint())).thenReturn(builder);
    when(builder.buildView(ComputeServiceContext.class)).thenReturn(serviceContext);
    when(serviceContext.getComputeService()).thenReturn(novaComputeService);

    when(novaComputeService.getContext()).thenReturn(serviceContext);
    when(serviceContext.unwrapApi(NovaApi.class)).thenReturn(novaApi);

    when(novaApi.getSecurityGroupApi(novaCredentials.getRegion())).thenReturn(securityGroupApiOptional);
    when(securityGroupApiOptional.isPresent()).thenReturn(true);
    when(securityGroupApiOptional.get()).thenReturn(securityGroupApi);

    when(novaApi.getKeyPairApi(novaCredentials.getRegion())).thenReturn(keyPairApiOptional);
    when(keyPairApiOptional.get()).thenReturn(keyPairApi);


  }


  @Test
  public void validateNovaCredentialsTest() throws InvalidNovaCredentialsException {
    NovaContext context = NovaLauncher.validateCredentials(novaCredentials, builder);
    assertNotNull(context);
    assertNotNull(context.getSecurityGroupApi());
  }

  @Test(expected = InvalidNovaCredentialsException.class)
  public void validateNovaCredentialsTestException() throws InvalidNovaCredentialsException {
    when(securityGroupApi.list()).thenThrow(AuthorizationException.class);
    NovaContext context = NovaLauncher.validateCredentials(novaCredentials, builder);
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
  public void createNovaLauncherTestNullContext() throws KaramelException {
    NovaLauncher launcher = new NovaLauncher(null, sshKeyPair);
  }

  @Test(expected = KaramelException.class)
  public void createNovaLauncherTestNullSSHKey() throws KaramelException {
    NovaLauncher launcher = new NovaLauncher(novaContext, null);
  }

  @Test
  public void createSecurityGroupTestWithTestingFlag() throws KaramelException {
    //Initializing and mocking need for method test
    SecurityGroupRule rule = mock(SecurityGroupRule.class);
    String uniqueGroup = NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName, groupName);
    String uniqueDescription = NovaSetting.NOVA_UNIQUE_GROUP_DESCRIPTION(clusterName, groupName);

    Ingress ingress = Ingress.builder()
            .fromPort(0)
            .toPort(65535)
            .ipProtocol(IpProtocol.TCP)
            .build();

    when(novaContext.getSecurityGroupApi()).thenReturn(securityGroupApi);
    when(securityGroupApi.createWithDescription(uniqueGroup, uniqueDescription)).thenReturn(securityGroupCreated);
    when(securityGroupCreated.getId()).thenReturn("10");
    when(securityGroupApi.createRuleAllowingCidrBlock("10", ingress, "0.0.0.0/0")).thenReturn(rule);

    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    String groupId = novaLauncher.createSecurityGroup(clusterName, groupName, nova, ports);
    assertEquals("10", groupId);
  }

  @Test
  public void uploadSSHPublicKeyAndCreate() throws KaramelException {
    String keypairName = "pepeKeyPair";
    KeyPair pair = mock(KeyPair.class);
    List<KeyPair> keyPairList = new ArrayList<>();
    FluentIterable<KeyPair> keys = FluentIterable.from(keyPairList);

    when(keyPairApi.list()).thenReturn(keys);
    when(keyPairApi.createWithPublicKey(keypairName, sshKeyPair.getPublicKey())).thenReturn(pair);

    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    boolean uploadSuccessful = novaLauncher.uploadSshPublicKey(keypairName, nova, false);
    assertTrue(uploadSuccessful);
  }

  @Test
  public void uploadSSHPublicKeyAndRecreateOld() throws KaramelException {
    String keypairName = "pepeKeyPair";
    KeyPair pair = mock(KeyPair.class);

    List<KeyPair> keyPairList = new ArrayList<>();
    keyPairList.add(pair);
    FluentIterable<KeyPair> keys = FluentIterable.from(keyPairList);

    when(keyPairApi.list()).thenReturn(keys);
    when(keyPairApi.delete(keypairName)).thenReturn(true);
    when(keyPairApi.createWithPublicKey(keypairName, sshKeyPair.getPublicKey())).thenReturn(pair);

    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    boolean uploadSuccessful = novaLauncher.uploadSshPublicKey(keypairName, nova, true);
    assertTrue(uploadSuccessful);
  }

  @Test
  public void uploadSSHPublicKeyAndNotRecreateOldFail() throws KaramelException {
    String keypairName = "pepeKeyPair";
    KeyPair pair = mock(KeyPair.class);

    List<KeyPair> keyPairList = new ArrayList<>();
    keyPairList.add(pair);
    FluentIterable<KeyPair> keys = FluentIterable.from(keyPairList);

    when(keyPairApi.list()).thenReturn(keys);
    when(keyPairApi.delete(keypairName)).thenReturn(true);
    when(keyPairApi.createWithPublicKey(keypairName, sshKeyPair.getPublicKey())).thenReturn(pair);

    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    boolean uploadSuccessful = novaLauncher.uploadSshPublicKey(keypairName, nova, false);
    assertFalse(uploadSuccessful);
  }

  @Test
  public void cleanupFailedNodesEmpty() throws KaramelException {
    Map<NodeMetadata, Throwable> failedNodes = new HashMap<>();
    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    boolean cleanupSuccessful = novaLauncher.cleanupFailedNodes(failedNodes);
    assertTrue(cleanupSuccessful);
  }

  @Test
  public void cleanupFailedNodesNotEmpty() throws KaramelException {
    Map<NodeMetadata, Throwable> failedNodes = new HashMap<>();
    Throwable exception = mock(Throwable.class);
    NodeMetadata meta = mock(NodeMetadata.class);
    failedNodes.put(meta, exception);

    Set<NodeMetadata> destroyedNodes = new HashSet<>();
    destroyedNodes.add(meta);
    when(meta.getId()).thenReturn("20");
    doReturn(destroyedNodes).when(novaComputeService).destroyNodesMatching(Predicates.in(failedNodes.keySet()));
    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    boolean cleanupSuccessful = novaLauncher.cleanupFailedNodes(failedNodes);
    assertTrue(cleanupSuccessful);
  }

  @Test
  public void cleanupFailedNodesSomethingWentWrong() throws KaramelException {
    Map<NodeMetadata, Throwable> failedNodes = new HashMap<>();
    Throwable exception = mock(Throwable.class);
    NodeMetadata meta = mock(NodeMetadata.class);
    failedNodes.put(meta, exception);

    Set<NodeMetadata> destroyedNodes = new HashSet<>();
    when(meta.getId()).thenReturn("20");
    doReturn(destroyedNodes).when(novaComputeService).destroyNodesMatching(Predicates.in(failedNodes.keySet()));
    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    boolean cleanupSuccessful = novaLauncher.cleanupFailedNodes(failedNodes);
    assertFalse(cleanupSuccessful);
  }

  @Test
  public void testForkGroup() throws KaramelException{
    //Same test parameters as the securityGroup Test
    //Initializing and mocking need for method test
    SecurityGroupRule rule = mock(SecurityGroupRule.class);
    String uniqueGroup = NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName, groupName);
    String uniqueDescription = NovaSetting.NOVA_UNIQUE_GROUP_DESCRIPTION(clusterName, groupName);

    Ingress ingress = Ingress.builder()
            .fromPort(0)
            .toPort(65535)
            .ipProtocol(IpProtocol.TCP)
            .build();

    when(novaContext.getSecurityGroupApi()).thenReturn(securityGroupApi);
    when(securityGroupApi.createWithDescription(uniqueGroup, uniqueDescription)).thenReturn(securityGroupCreated);
    when(securityGroupCreated.getId()).thenReturn("10");
    when(securityGroupApi.createRuleAllowingCidrBlock("10", ingress, "0.0.0.0/0")).thenReturn(rule);

    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    //String groupId = novaLauncher.createSecurityGroup(clusterName, groupName, nova, ports);

    JsonCluster cluster = mock(JsonCluster.class);
    ClusterRuntime clusterRuntime = mock(ClusterRuntime.class);
    List<JsonGroup> groups = new ArrayList<>();
    JsonGroup group = mock(JsonGroup.class);
    groups.add(group);
    when(group.getName()).thenReturn(groupName);
    when(cluster.getGroups()).thenReturn(groups);
    when(group.getProvider()).thenReturn(nova);
    when(cluster.getProvider()).thenReturn(nova);
    when(cluster.getName()).thenReturn(clusterName);
    String groupId = novaLauncher.forkGroup(cluster,clusterRuntime,groupName);

    assertEquals("10", groupId);
  }

  @Ignore
  @Test
  public void testForkMachines() throws KaramelException, RunNodesException {
    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    //mocking uploadSSHPublicKey
    String keypairName = "pepeKeyPair";
    KeyPair pair = mock(KeyPair.class);

    List<KeyPair> keyPairList = new ArrayList<>();
    keyPairList.add(pair);
    FluentIterable<KeyPair> keys = FluentIterable.from(keyPairList);

    when(keyPairApi.list()).thenReturn(keys);
    when(keyPairApi.delete(keypairName)).thenReturn(true);
    when(keyPairApi.createWithPublicKey(keypairName, sshKeyPair.getPublicKey())).thenReturn(pair);

    //mocking
    JsonCluster cluster = mock(JsonCluster.class);
    ClusterRuntime clusterRuntime = mock(ClusterRuntime.class);
    when(clusterRuntime.getName()).thenReturn(clusterName);
    List<JsonGroup> groups = new ArrayList<>();

    //mocking json group
    JsonGroup group = mock(JsonGroup.class);
    groups.add(group);
    when(group.getName()).thenReturn(groupName);
    when(group.getProvider()).thenReturn(nova);
    when(group.getSize()).thenReturn(1);

    //mocking json cluster
    when(cluster.getGroups()).thenReturn(groups);
    when(cluster.getProvider()).thenReturn(nova);
    when(cluster.getName()).thenReturn(clusterName);

    //mocking group runtime
    List<GroupRuntime> groupRuntimes = new ArrayList<>();
    GroupRuntime groupRuntime = mock(GroupRuntime.class);
    when(groupRuntime.getName()).thenReturn(groupName);
    when(groupRuntime.getId()).thenReturn("10");
    when(groupRuntime.getCluster()).thenReturn(clusterRuntime);
    groupRuntimes.add(groupRuntime);

    //mocking clusterRuntime
    when(clusterRuntime.getGroups()).thenReturn(groupRuntimes);

    //mocking templateOptions
    NovaTemplateOptions novaTemplateOptions = mock(NovaTemplateOptions.class);
    TemplateBuilder templateBuilder = mock(TemplateBuilder.class);

    TemplateOptions templateOptions = mock(TemplateOptions.class);

    when(novaContext.getComputeService()).thenReturn(novaComputeService);
    when(novaComputeService.templateOptions()).thenReturn(novaTemplateOptions);

    when(novaTemplateOptions.securityGroups(Matchers.anyCollection())).thenReturn(novaTemplateOptions);
    when(templateOptions.as(NovaTemplateOptions.class)).thenReturn(novaTemplateOptions);
    when(novaComputeService.templateBuilder()).thenReturn(templateBuilder);

    //mock builder
    when(novaTemplateOptions.keyPairName(keypairName)).thenReturn(novaTemplateOptions);
    when(novaTemplateOptions.autoAssignFloatingIp(true)).thenReturn(novaTemplateOptions);
    when(novaTemplateOptions.nodeNames(Matchers.anyCollection())).thenReturn(novaTemplateOptions);

    //mock success nodes
    Set<NodeMetadata> succeededNodes = new HashSet<>();
    NodeMetadata succeededNode = mock(NodeMetadata.class);

    succeededNodes.add(succeededNode);
    doReturn(succeededNodes).when(novaComputeService)
            .createNodesInGroup(Matchers.anyString(), eq(1), Matchers.any(Template.class));

    LoginCredentials loginCredentials = mock(LoginCredentials.class);
    Set<String> ipAddresses = new HashSet<>();
    ipAddresses.add("127.0.0.1");
    when(succeededNode.getPublicAddresses()).thenReturn(ipAddresses);
    when(succeededNode.getPrivateAddresses()).thenReturn(ipAddresses);
    when(succeededNode.getLoginPort()).thenReturn(22);
    when(succeededNode.getCredentials()).thenReturn(loginCredentials);
    when(loginCredentials.getUser()).thenReturn("ubuntu");

    //testing method
    List<NodeRunTime> forkedMachines =novaLauncher.forkMachines(cluster,clusterRuntime,groupName);

    assertNotNull(forkedMachines);
    assertFalse(forkedMachines.isEmpty());
  }

  @Test
  public void cleanup() throws KaramelException{
    String uniqueGroup = NovaSetting.NOVA_UNIQUE_GROUP_NAME(clusterName, groupName);

    //mocking
    JsonCluster cluster = mock(JsonCluster.class);
    ClusterRuntime clusterRuntime = mock(ClusterRuntime.class);
    when(clusterRuntime.getName()).thenReturn(clusterName);

    List<JsonGroup> groups = new ArrayList<>();
    JsonGroup group = mock(JsonGroup.class);
    groups.add(group);
    when(cluster.getGroups()).thenReturn(groups);
    when(cluster.getProvider()).thenReturn(nova);
    when(cluster.getName()).thenReturn(clusterName);

    //mocking json group
    when(group.getName()).thenReturn(groupName);
    when(group.getProvider()).thenReturn(nova);
    when(group.getSize()).thenReturn(1);

    //mocking group runtime
    List<GroupRuntime> groupRuntimes = new ArrayList<>();
    GroupRuntime groupRuntime = mock(GroupRuntime.class);
    when(groupRuntime.getName()).thenReturn(groupName);
    when(groupRuntime.getId()).thenReturn("10");
    when(groupRuntime.getCluster()).thenReturn(clusterRuntime);
    groupRuntimes.add(groupRuntime);

    //mocking clusterRuntime
    when(clusterRuntime.getGroups()).thenReturn(groupRuntimes);

    //mocking securityGroups
    SecurityGroup securityGroup = mock(SecurityGroup.class);
    List<SecurityGroup> securityGroupList = new ArrayList<>();
    securityGroupList.add(securityGroup);
    FluentIterable<SecurityGroup> securityGroupFluentIterable = FluentIterable.from(securityGroupList);

    when(novaContext.getSecurityGroupApi()).thenReturn(securityGroupApi);
    when(securityGroupApi.list()).thenReturn(securityGroupFluentIterable);
    when(securityGroup.getName()).thenReturn(uniqueGroup);

    NovaLauncher novaLauncher = new NovaLauncher(novaContext, sshKeyPair);
    novaLauncher.cleanup(cluster, clusterRuntime);
  }
}