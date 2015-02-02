/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.launcher.amazon;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jclouds.aws.AWSResponseException;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.predicates.NodePredicates;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jclouds.ec2.domain.KeyPair;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.ec2.features.SecurityGroupApi;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.backend.running.model.GroupEntity;
import se.kth.karamel.backend.running.model.MachineEntity;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.client.model.Ec2;

/**
 * @author kamal
 */
public final class Ec2Launcher {

  private static final Logger logger = Logger.getLogger(Ec2Launcher.class);
  public static boolean TESTING = true;
  public static Ec2Context context;

  public static boolean validateAndUpdateCredentials(String account, String accessKey) {
    try {
      Ec2Context cxt = new Ec2Context(account, accessKey);
      SecurityGroupApi securityGroupApi = cxt.getSecurityGroupApi();
      securityGroupApi.describeSecurityGroupsInRegion(Settings.PROVIDER_EC2_DEFAULT_REGION);
      context = cxt;
      return true;
    } catch (AuthorizationException e) {
      context = null;
      return false;
    }
  }

  public static void createSecurityGroup(String clusterName, String groupName, String region, Set<String> ports) throws KaramelException {
    logger.info(String.format("Creating security group '%s' ...", groupName));
    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    Optional<? extends org.jclouds.ec2.features.SecurityGroupApi> securityGroupExt
            = context.getEc2api().getSecurityGroupApiForRegion(region);
    if (securityGroupExt.isPresent()) {
      SecurityGroupApi client = securityGroupExt.get();
      client.createSecurityGroupInRegion(region, groupName, "A region for cluster '" + clusterName + "'");

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
          client.authorizeSecurityGroupIngressInRegion(region,
                  groupName, pr, p, Integer.valueOf(port), "0.0.0.0/0");
          logger.info(String.format("Ports became open for '%s'", groupName));
        }
      } else {
        client.authorizeSecurityGroupIngressInRegion(region,
                groupName, IpProtocol.TCP, 0, 65535, "0.0.0.0/0");
        logger.info(String.format("Ports became open for '%s'", groupName));
      }
      logger.info(String.format("Security group '%s' was created :)", groupName));
    }
  }

  public static List<MachineEntity> forkMachines(GroupEntity mainGroup, Set<String> securityGroupNames, int size, Ec2 ec2) throws KaramelException {
    logger.info(String.format("Forking %d machines for '%s' ...", size, mainGroup.getName()));
    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }

    EC2TemplateOptions options = context.getComputeService().templateOptions().as(EC2TemplateOptions.class);
    HashSet<String> regions = new HashSet();
    if (!regions.contains(ec2.getRegion())) {
      Confs confs = Confs.loadConfs();
      Set<KeyPair> keypairs = context.getKeypairApi().describeKeyPairsInRegion(ec2.getRegion(), new String[]{Settings.EC2_KEYPAIR_NAME});
      if (keypairs.isEmpty()) {
        context.getKeypairApi().importKeyPairInRegion(ec2.getRegion(), Settings.EC2_KEYPAIR_NAME, confs.getProperty(Settings.SSH_PUBKEY_KEY));
      }
      regions.add(ec2.getRegion());
    }
    TemplateBuilder template = context.getComputeService().templateBuilder();
    options.keyPair(Settings.EC2_KEYPAIR_NAME);
    options.as(EC2TemplateOptions.class).securityGroups(securityGroupNames);
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
                mainGroup.getName(), size, template.build());
        logger.info(String.format("Cool!! we got %d machine(s) for'%s' |;-)", size, mainGroup.getName()));
      } catch (IllegalStateException | RunNodesException ex) {
        logger.info(String.format("#%d Hurry up EC2!! I want machines for %s, will ask you again in %d ms :@", tries, mainGroup.getName(), Settings.EC2_RETRY_INTERVAL), ex);
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
      List<MachineEntity> machines = new ArrayList<>();
      for (NodeMetadata node : forkedNodes) {
        MachineEntity machine = new MachineEntity(mainGroup);
        if (node != null) {
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

  public static void cleanup(String groupName, String region) throws KaramelException {
    logger.info(String.format("Destroying security group '%s' and all its machines...", groupName));
    if (context == null) {
      throw new KaramelException("Register your valid credentials first :-| ");
    }
    for (SecurityGroup secgroup : context.getSecurityGroupApi().describeSecurityGroupsInRegion(region)) {
      if (secgroup.getName().startsWith("jclouds#" + groupName) || secgroup.getName().equals(groupName)) {
        context.getComputeService().destroyNodesMatching(NodePredicates.inGroup(groupName));
        logger.info(String.format("Machines destroyed in the security group '%s'.", secgroup.getName()));
      }
    }
    for (SecurityGroup secgroup : context.getSecurityGroupApi().describeSecurityGroupsInRegion(region)) {
      if (secgroup.getName().startsWith("jclouds#" + groupName) || secgroup.getName().equals(groupName)) {
        boolean retry = false;
        int count = 0;
        do {
          count++;
          try {
            logger.info(String.format("#%d Destroying security group '%s' ...", count, secgroup.getName()));
            context.getSecurityGroupApi().deleteSecurityGroupInRegion(region, secgroup.getName());
          } catch (IllegalStateException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof AWSResponseException) {
              AWSResponseException e = (AWSResponseException) cause;
              if (e.getError().getCode().equals("InvalidGroup.InUse")) {
                logger.info(String.format("Hurry up EC2!! terminate machines!! '%s', will retry in %d ms :@", groupName, Settings.EC2_RETRY_INTERVAL));
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
