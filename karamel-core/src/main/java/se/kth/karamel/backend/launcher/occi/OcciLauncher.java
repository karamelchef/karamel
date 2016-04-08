package se.kth.karamel.backend.launcher.occi;

import cz.cesnet.cloud.occi.Model;
import cz.cesnet.cloud.occi.api.Client;
import cz.cesnet.cloud.occi.api.EntityBuilder;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.exception.EntityBuildingException;
import cz.cesnet.cloud.occi.api.http.HTTPClient;
import cz.cesnet.cloud.occi.api.http.auth.HTTPAuthentication;
import cz.cesnet.cloud.occi.api.http.auth.VOMSAuthentication;
import cz.cesnet.cloud.occi.core.Entity;
import cz.cesnet.cloud.occi.core.Link;
import cz.cesnet.cloud.occi.core.Resource;
import cz.cesnet.cloud.occi.exception.AmbiguousIdentifierException;
import cz.cesnet.cloud.occi.exception.InvalidAttributeValueException;
import cz.cesnet.cloud.occi.infrastructure.Compute;
import cz.cesnet.cloud.occi.infrastructure.IPNetworkInterface;
import cz.cesnet.cloud.occi.infrastructure.NetworkInterface;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import se.kth.karamel.backend.launcher.Launcher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.exception.InvalidOcciCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.util.OcciCredentials;
import se.kth.karamel.common.util.SshKeyPair;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Occi;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.util.Settings;


/**
 * Created by Mamut on 2016-1-19.
 * Class required to launch machines in OCCI compatible clouds
 */
public final class OcciLauncher extends Launcher{
    
  private static final int DEFAULT_SSH_PORT = 22;  
  private static final Logger logger = Logger.getLogger(OcciLauncher.class);
  public final OcciContext context;
  private static boolean TESTING = true;  
  private final SshKeyPair sshKeyPair;
  
  /**
   *
   * @param occiContext
   * @param sshKeyPair
   */
  public OcciLauncher(OcciContext occiContext, SshKeyPair sshKeyPair) {
    this.context = occiContext;
    this.sshKeyPair = sshKeyPair;
  }

  /**
   * Dummy validate function
   * @param occiCredentials
   * @return
   * @throws InvalidOcciCredentialsException
   */
  public static OcciContext validateCredentials(OcciCredentials occiCredentials)
          throws InvalidOcciCredentialsException {
    OcciContext context = new OcciContext(occiCredentials);
      
    //basic path and file existence check
    File f = new File(context.getOcciCredentials().getUserCertificatePath());    
    if((!f.exists() || f.isDirectory())) {
      logger.info(String.format("Certificate does not exist in provided path " + 
              context.getOcciCredentials().getUserCertificatePath()));
      throw new InvalidOcciCredentialsException("Certificate does not exist in provided path " + 
              context.getOcciCredentials().getUserCertificatePath());  
    }
      
    f = new File(context.getOcciCredentials().getSystemCertDir());
    if ((!f.exists() || !f.isDirectory())) {
      logger.info(String.format("Certificate folder does not exist in provided path " + 
              context.getOcciCredentials().getSystemCertDir()));
      throw new InvalidOcciCredentialsException("Certificate folder does not exist in provided path " + 
              context.getOcciCredentials().getSystemCertDir());
    }
       
    return context;
  }

  /**
   * Function loads settings stored on local store from previous usage
   * @param confs
   * @return 
   */
  public static OcciCredentials readCredentials(Confs confs) {
    String userCertificatePath = confs.getProperty("occi.user.certificate.path");
    String systemCertDir = confs.getProperty("occi.certificate.dir");
    if (userCertificatePath == null || userCertificatePath.isEmpty()) {
      userCertificatePath = Settings.OCCI_USER_CERTIFICATE_PATH;
    }
    if (systemCertDir == null || systemCertDir.isEmpty()) {
      systemCertDir = Settings.OCCI_CERTIFICATE_DIR;
    }
    
    OcciCredentials occiCredentials = null;
    if (userCertificatePath != null && !userCertificatePath.isEmpty() 
            && systemCertDir != null && !systemCertDir.isEmpty()) {
      occiCredentials = new OcciCredentials();
      occiCredentials.setUserCertificatePath(userCertificatePath);
      occiCredentials.setSystemCertDir(systemCertDir);
    }
    return occiCredentials;
  }
  
  @Override
  public void cleanup(JsonCluster definition, ClusterRuntime runtime) throws KaramelException {
    List<GroupRuntime> groups = runtime.getGroups();

    for (GroupRuntime group : groups) {
      group.getCluster().resolveFailures();
      Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      Occi occi = (Occi) provider;
      if (provider instanceof Occi) {
        //List all VM ids - compute resource URIs
        List<String> allOcciVmsIds = new ArrayList<>();;
        for (MachineRuntime machine : group.getMachines()) {
          if (machine.getName() != null) {
            //compute resource location URI (vms to delete)
            allOcciVmsIds.add(machine.getVmId());
          }
        }
        
        try {        
          //Cleanup Vms
          logger.info(String.format("Killing following machines with ids: " + "%s", allOcciVmsIds));
          HTTPAuthentication authentication = new VOMSAuthentication(context.getOcciCredentials()
             .getUserCertificatePath());
          //set custom certificates if needed
          authentication.setCAPath(context.getOcciCredentials().getSystemCertDir());
          Client client = new HTTPClient(URI.create(occi.getOcciEndpoint()), authentication);
          //Destroy Vms in Occi
          for (String VmId : allOcciVmsIds) { 
            logger.info(String.format("Deleting compute : " + VmId));
            boolean done = client.delete(URI.create(VmId));
            if (done) {
              logger.info(String.format("Deleted."));
            } else {
              logger.info(String.format("Not deleted."));
            }
          }
        }
        catch(Throwable ex) {
          logger.info(String.format(ex.toString()));
          throw new KaramelException("Occi Cleanup fail", ex);
        }
      }
    }  
  }

  /**
   * Function tries to create VM on Occi cloud defined by client and occiImage
   * @param client
   * @param vmName
   * @param occiImage
   * @param occiImageSize
   * @param SSHPublicKey
   * @return
   * @throws InvalidAttributeValueException
   * @throws EntityBuildingException
   * @throws AmbiguousIdentifierException
   * @throws se.kth.karamel.common.exception.KaramelException
   */
  public URI createCompute(Client client, String vmName, String occiImage, String occiImageSize, String SSHPublicKey ) 
          throws AmbiguousIdentifierException, KaramelException, 
          InvalidAttributeValueException, EntityBuildingException {
     
    Model model;
    EntityBuilder eb;
    URI location = null;

    //GET MODEL AND ENTITYBUILDER
    model = client.getModel();
    //logger.info("Model Occi: " + String.format(model.toString()));
    
    eb = new EntityBuilder(model);
    Compute compute = eb.getCompute();
    compute.setTitle(vmName);
    compute.setHostname(vmName);
    try{
      logger.info(String.format("occi Image:" + occiImage));
      //compute.addMixin(model.findMixin(occiImage));
      compute.addMixin(model.findMixin(URI.create(occiImage)));
    }
    catch (Throwable ex) {
      logger.error(String.format("Occi image mixin " + occiImage + "not found (did you chose existing one?"));
      throw new KaramelException("occi image mixin not found (did you chose existing one?)", ex);
    }
    
    try{
      logger.info(String.format("occi ImageSize:" + occiImageSize));
      //compute.addMixin(model.findMixin(occiImageSize));
      compute.addMixin(model.findMixin(URI.create(occiImageSize)));  
    }
    catch  (Throwable ex) {
      logger.error(String.format("Occi image mixin " + occiImageSize + "not found (did you chose existing one?"));
      throw new KaramelException("occi image size mixin not found (did you chose existing one?)", ex);
    }
    //Pub key addition to VM for root ssh
    compute.addMixin(model.findMixin("public_key"));
    compute.addAttribute("org.openstack.credentials.publickey.name", "my_key");     
    compute.addAttribute("org.openstack.credentials.publickey.data", this.sshKeyPair.getPublicKey());   
    logger.info(String.format("Creating VM :" + vmName));
    try{
      location = client.create(compute);
    } catch (CommunicationException ex) {
      logger.info(String.format("OCCI create compute failed"));
      throw new KaramelException("OCCI create compute failed", ex);
    }
    return location; 
  }
  
  /**
   * Get compute resource public IPs
   * @param client
   * @param location
   * @return Returns public IP addresses from compute resource (VM) specified by URI location
   * @throws CommunicationException
   * @throws UnknownHostException
   */
  public ArrayList<String> getPublicIPs (Client client, URI location) 
          throws CommunicationException, UnknownHostException{
    //Get private and public IP addresses
    ArrayList<String> publicIps = new ArrayList();
    List<Entity> entities = client.describe(location);       
    for (Entity entity : entities) {
      Resource resource = (Resource) entity;
      Set<Link> links = resource.getLinks(NetworkInterface.TERM_DEFAULT);
      for (Link link : links) {        
        String address = link.getValue(IPNetworkInterface.ADDRESS_ATTRIBUTE_NAME);
        if (!InetAddress.getByName(address).isSiteLocalAddress()) {
          publicIps.add(address);
        }
      }
    }
    return publicIps;
  }
    
  @Override
  public String forkGroup(JsonCluster definition, ClusterRuntime runtime, String groupName) throws KaramelException {
    return groupName;
  }  
  
  @Override
  public List<MachineRuntime> forkMachines(JsonCluster definition, ClusterRuntime runtime, String groupName)
          throws KaramelException {

    Occi occi = (Occi) UserClusterDataExtractor.getGroupProvider(definition, groupName);
    JsonGroup definedGroup = UserClusterDataExtractor.findGroup(definition, groupName);
    GroupRuntime group = UserClusterDataExtractor.findGroup(runtime, groupName);
    
    //log details
    logger.info(String.format("Occi ForkMachines  ..."));
    logger.info(String.format("Provider of groupName %s is occi.", groupName));  
    logger.info(String.format("Cert path '%s'  ...", 
            context.getOcciCredentials().getUserCertificatePath()));
    logger.info(String.format("Cert Dir path '%s'  ...", 
            context.getOcciCredentials().getSystemCertDir()));
    logger.info(String.format("Occi Image "+ occi.getOcciImage()));
    logger.info(String.format("Occi ImageSize "+ occi.getOcciImageSize()));
    logger.info(String.format("Occi Endpoint "+ occi.getOcciEndpoint()));
    
    logger.info(String.format("Occi ForkMachines Cert path: " + 
            context.getOcciCredentials().getUserCertificatePath()));
    logger.info(String.format("Occi ForkMachines Cert Dir path: " + 
            context.getOcciCredentials().getSystemCertDir()));
    logger.info(String.format("Occi ForkMachines Machine Num in Group: " + 
            definedGroup.getSize()));
        
    try {
      HTTPAuthentication authentication = new VOMSAuthentication(context.getOcciCredentials()
        .getUserCertificatePath());
      //set custom certificates
      authentication.setCAPath(context.getOcciCredentials().getSystemCertDir());
      logger.info(String.format("Connecting to endpoint " + occi.getOcciEndpoint()));      
      Client client = new HTTPClient(URI.create(occi.getOcciEndpoint()), authentication);     
      //connect client
      client.connect();
      
      //FORK THE MACHINES IN GROUP
      int numForked = 0;
      final int numMachines = definedGroup.getSize();
      List<MachineRuntime> machines = new ArrayList<>();
      URI location;
              
      while (numForked < numMachines) {
        //CREATE COMPUTES and store their URI location
        String vmName = groupName + numForked;
        location = this.createCompute(client, vmName, occi.getOcciImage(), 
                occi.getOcciImageSize(), this.sshKeyPair.getPublicKey());
 
        //Construct machine runtime list
        MachineRuntime machine = new MachineRuntime(group);
        machine.setMachineType("occi/" + occi.getOcciEndpoint() + "/" + occi.getOcciImage()); 
        ArrayList<String> publicIps = this.getPublicIPs(client, location);
        machine.setPublicIp(publicIps.isEmpty() ? "" : publicIps.get(0));
        machine.setPrivateIp("");
        logger.info(String.format("Public IP of " + vmName + " is " + (publicIps.isEmpty() ? "" : publicIps.get(0))));
        machine.setVmId(location.toString());
        machine.setName(vmName);
        machine.setSshPort(DEFAULT_SSH_PORT);
        machine.setSshUser(occi.getUsername());      
        logger.info(String.format("VM username: " + occi.getUsername()));      
        machines.add(machine);
        numForked++;
      }     
      return machines;
    }
    catch(Throwable ex) {
      logger.info(String.format(ex.toString()));
      throw new KaramelException("Occi ForkMachines ", ex);
    }
  }
}
