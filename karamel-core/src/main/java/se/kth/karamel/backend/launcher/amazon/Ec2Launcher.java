/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.launcher.amazon;

import com.google.common.base.Optional;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Predicate;
import static com.google.common.base.Strings.emptyToNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jclouds.aws.AWSResponseException;
import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.aws.ec2.features.AWSSecurityGroupApi;
import org.jclouds.aws.ec2.options.CreateSecurityGroupOptions;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.predicates.NodePredicates;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jclouds.ec2.domain.KeyPair;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.ec2.features.SecurityGroupApi;
import org.jclouds.ec2.util.IpPermissions;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.client.model.Ec2;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.InvalidEc2CredentialsException;

/**
 * @author kamal
 */
public final class Ec2Launcher {

  private static final Logger logger = Logger.getLogger(Ec2Launcher.class);
  public static boolean TESTING = true;
  public final Ec2Context context;
  public final SshKeyPair sshKeyPair;

  public Ec2Launcher(Ec2Context context, SshKeyPair sshKeyPair) {
    this.context = context;
    this.sshKeyPair = sshKeyPair;
    logger.info(String.format("Account-id='%s'", context.getCredentials().getAccountId()));
    logger.info(String.format("Public-key='%s'", sshKeyPair.getPublicKeyPath()));
    logger.info(String.format("Private-key='%s'", sshKeyPair.getPrivateKeyPath()));
  }

  public static Ec2Context validateCredentials(Ec2Credentials credentials) throws InvalidEc2CredentialsException {
    try {
      Ec2Context cxt = new Ec2Context(credentials);
      SecurityGroupApi securityGroupApi = cxt.getSecurityGroupApi();
      securityGroupApi.describeSecurityGroupsInRegion(Settings.PROVIDER_EC2_DEFAULT_REGION);
      return cxt;
    } catch (AuthorizationException e) {
      throw new InvalidEc2CredentialsException("accountid:" + credentials.getAccountId(), e);
    }
  }

  public static Ec2Credentials readCredentials(Confs confs) {
    String accountId = confs.getProperty(Settings.EC2_ACCOUNT_ID_KEY);
    String accessKey = confs.getProperty(Settings.EC2_ACCESSKEY_KEY);
    Ec2Credentials credentials = null;
    if (accountId != null && !accountId.isEmpty() && accessKey != null && !accessKey.isEmpty()) {
      credentials = new Ec2Credentials();
      credentials.setAccountId(accountId);
      credentials.setAccessKey(accessKey);

    }
    return credentials;
  }

  public String createSecurityGroup(String clusterName, JsonGroup group, Set<String> ports) throws KaramelException {
    String uniqeGroupName = Settings.EC2_UNIQUE_GROUP_NAME(clusterName, group.getName());
    logger.info(String.format("Creating security group '%s' ...", uniqeGroupName));
    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    }

    Optional<? extends org.jclouds.ec2.features.SecurityGroupApi> securityGroupExt
            = context.getEc2api().getSecurityGroupApiForRegion(group.getEc2().getRegion());
    if (securityGroupExt.isPresent()) {
      AWSSecurityGroupApi client = (AWSSecurityGroupApi) securityGroupExt.get();
      String groupId = null;
      if (group.getEc2().getVpc() != null) {
        CreateSecurityGroupOptions csgos = CreateSecurityGroupOptions.Builder.vpcId(group.getEc2().getVpc());
        groupId = client.createSecurityGroupInRegionAndReturnId(group.getEc2().getRegion(), uniqeGroupName, uniqeGroupName, csgos);
      } else {
        groupId = client.createSecurityGroupInRegionAndReturnId(group.getEc2().getRegion(), uniqeGroupName, uniqeGroupName);
      }

      if (!TESTING) {
        for (String port : ports) {
          Integer p = null;
          IpProtocol pr = null;
          if (port.contains("/")) {
            String[] s = port.split("/");
            p = Integer.valueOf(s[0]);
            pr = IpProtocol.valueOf(s[1]);
          } else {
            p = Integer.valueOf(port);
            pr = IpProtocol.TCP;
          }
          client.authorizeSecurityGroupIngressInRegion(group.getEc2().getRegion(),
                  uniqeGroupName, pr, p, Integer.valueOf(port), "0.0.0.0/0");
          logger.info(String.format("Ports became open for '%s'", uniqeGroupName));
        }
      } else {
        IpPermission ippermission = IpPermission.builder().ipProtocol(IpProtocol.TCP).fromPort(0).toPort(65535).cidrBlock("0.0.0.0/0").build();
        client.authorizeSecurityGroupIngressInRegion(group.getEc2().getRegion(), groupId, ippermission);
        logger.info(String.format("Ports became open for '%s'", uniqeGroupName));
      }
      logger.info(String.format("Security group '%s' was created :)", uniqeGroupName));
      return groupId;
    }
    return null;
  }

  public void uploadSshPublicKey(String keyPairName, Ec2 ec2, boolean removeOld) throws KaramelException {
    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    }

    HashSet<String> regions = new HashSet();
    if (!regions.contains(ec2.getRegion())) {
      Set<KeyPair> keypairs = context.getKeypairApi().describeKeyPairsInRegion(ec2.getRegion(), new String[]{keyPairName});
      if (keypairs.isEmpty()) {
        logger.info(String.format("New keypair '%s' is being uploaded to EC2", keyPairName));
        context.getKeypairApi().importKeyPairInRegion(ec2.getRegion(), keyPairName, sshKeyPair.getPublicKey());
      } else {
        if (removeOld) {
          logger.info(String.format("Removing the old keypair '%s' and uploading the new one ...", keyPairName));
          context.getKeypairApi().deleteKeyPairInRegion(ec2.getRegion(), keyPairName);
          context.getKeypairApi().importKeyPairInRegion(ec2.getRegion(), keyPairName, sshKeyPair.getPublicKey());
        } 
      }
      regions.add(ec2.getRegion());
    }
  }

  public List<MachineRuntime> forkMachines(String keyPairName, GroupRuntime mainGroup,
          Set<String> securityGroupIds, int size, Ec2 ec2) throws KaramelException {
    String uniqeGroupName = Settings.EC2_UNIQUE_GROUP_NAME(mainGroup.getCluster().getName(), mainGroup.getName());
    List<String> uniqeVmNames = Settings.EC2_UNIQUE_VM_NAMES(mainGroup.getCluster().getName(), mainGroup.getName(), size);
    logger.info(String.format("Forking %d machines for '%s' ...", size, uniqeGroupName));

    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    }
    AWSEC2TemplateOptions options = context.getComputeService().templateOptions().as(AWSEC2TemplateOptions.class);
    if (ec2.getPrice() != null) {
      options.spotPrice(ec2.getPrice());
    }

    TemplateBuilder template = context.getComputeService().templateBuilder();
    options.keyPair(keyPairName);
    options.as(AWSEC2TemplateOptions.class).securityGroupIds(securityGroupIds);

    options.nodeNames(uniqeVmNames);
    if (ec2.getSubnet() != null) {
      options.as(AWSEC2TemplateOptions.class).subnetId(ec2.getSubnet());
    }
    template.options(options);
    template.os64Bit(true);
    template.hardwareId(ec2.getType());
    template.imageId(ec2.getRegion() + "/" + ec2.getImage());
    template.locationId(ec2.getRegion());
    boolean succeed = false;
    int tries = 0;
    Set<? extends NodeMetadata> forkedNodes = null;
    while (!succeed && tries < Settings.EC2_RETRY_MAX) {
      succeed = true;
      tries++;
      try {
        forkedNodes = context.getComputeService().createNodesInGroup(
                uniqeGroupName, size, template.build());
        logger.info(String.format("Cool!! we got %d machine(s) for'%s' |;-)", size, uniqeGroupName));
      } catch (IllegalStateException | RunNodesException ex) {
        logger.info(String.format("#%d Hurry up EC2!! I want machines for %s, will ask you again in %d ms :@", tries, uniqeGroupName, Settings.EC2_RETRY_INTERVAL), ex);
      }

      if (forkedNodes == null) {
        try {
          Thread.currentThread().sleep(Settings.EC2_RETRY_INTERVAL);
        } catch (InterruptedException ex1) {
          logger.error("", ex1);
        }
      }

    }

    if (forkedNodes != null) {
      List<MachineRuntime> machines = new ArrayList<>();
      for (NodeMetadata node : forkedNodes) {
        if (node != null) {
          MachineRuntime machine = new MachineRuntime(mainGroup);
          ArrayList<String> privateIps = new ArrayList();
          ArrayList<String> publicIps = new ArrayList();
          privateIps.addAll(node.getPrivateAddresses());
          publicIps.addAll(node.getPublicAddresses());
          machine.setPrivateIp(privateIps.get(0));
          machine.setPublicIp(publicIps.get(0));
          machine.setSshPort(node.getLoginPort());
          machine.setSshUser(node.getCredentials().getUser());
          machines.add(machine);
        }
      }
      return machines;
    }
    throw new KaramelException(String.format("Couldn't fork machines for group'%s'", mainGroup.getName()));
  }

  public void cleanup(String clusterName, List<String> vmNames, Map<String, String> groupRegion) throws KaramelException {
    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    if (sshKeyPair == null) {
      throw new KaramelException("Choose your ssh keypair first :-| ");
    }

    logger.info(String.format("Killing following machines in the cluster if exist..\n %s", vmNames.toString()));
    context.getComputeService().destroyNodesMatching(withNamePrefix(vmNames));
    logger.info(String.format("All machines destroyed in all the security groups. :) "));
    for (Map.Entry<String, String> gp : groupRegion.entrySet()) {
      String uniqueGroupName = Settings.EC2_UNIQUE_GROUP_NAME(clusterName, gp.getKey());
      for (SecurityGroup secgroup : context.getSecurityGroupApi().describeSecurityGroupsInRegion(gp.getValue())) {
        if (secgroup.getName().startsWith("jclouds#" + uniqueGroupName) || secgroup.getName().equals(uniqueGroupName)) {
          logger.info(String.format("Destroying security group '%s' ...", secgroup.getName()));
          boolean retry = false;
          int count = 0;
          do {
            count++;
            try {
              logger.info(String.format("#%d Destroying security group '%s' ...", count, secgroup.getName()));
              ((AWSSecurityGroupApi) context.getSecurityGroupApi()).deleteSecurityGroupInRegionById(gp.getValue(), secgroup.getId());
            } catch (IllegalStateException ex) {
              Throwable cause = ex.getCause();
              if (cause instanceof AWSResponseException) {
                AWSResponseException e = (AWSResponseException) cause;
                if (e.getError().getCode().equals("InvalidGroup.InUse") || e.getError().getCode().equals("DependencyViolation")) {
                  logger.info(String.format("Hurry up EC2!! terminate machines!! '%s', will retry in %d ms :@", uniqueGroupName, Settings.EC2_RETRY_INTERVAL));
                  retry = true;
                  try {
                    Thread.currentThread().sleep(Settings.EC2_RETRY_INTERVAL);
                  } catch (InterruptedException ex1) {
                    logger.error("", ex1);
                  }
                } else {
                  throw ex;
                }
              }
            }
          } while (retry);
          logger.info(String.format("The security group '%s' destroyed ^-^", secgroup.getName()));
        }
      }
    }
  }

  public static Predicate<NodeMetadata> withNamePrefix(final List<String> namePrefixs) {
    return new Predicate<NodeMetadata>() {
      @Override
      public boolean apply(NodeMetadata nodeMetadata) {
        String name = nodeMetadata.getName();
        if (name == null) {
          return false;
        } else {
          for (String pref : namePrefixs) {
            if (name.equals(pref)) {
              return true;
            }
          }
        }
        return false;
      }

      @Override
      public String toString() {
        return "nameStartsWith(" + namePrefixs.toArray().toString() + ")";
      }
    };
  }
}
