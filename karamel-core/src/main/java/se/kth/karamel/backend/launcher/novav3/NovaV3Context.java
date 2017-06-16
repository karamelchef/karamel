package se.kth.karamel.backend.launcher.novav3;

//import com.google.common.collect.ImmutableSet;
//import com.google.inject.Module;

//import org.jclouds.ContextBuilder;

import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.compute.ComputeService;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.model.identity.v3.Token;
import java.util.Date;

/*
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.config.NovaProperties;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
*/
import org.apache.log4j.Logger;

//import org.jclouds.sshj.config.SshjSshClientModule;
import se.kth.karamel.common.util.NovaCredentials;
import java.util.Properties;

/**
 * Nova Context
 * Created by Alberto on 2015-05-16.
 */
public class NovaV3Context {
  private static final Logger logger = Logger.getLogger(NovaV3Launcher.class);
  private final NovaCredentials novaCredentials;
  private final IOSClientBuilder.V3 builder;
  private OSClientV3 os = null;
  private Token token;

  public NovaV3Context(NovaCredentials credentials) {
    this.novaCredentials = credentials;
    this.builder = buildContext(credentials);
  }

  public static IOSClientBuilder.V3 buildContext(NovaCredentials credentials) {
    Properties properties = new Properties();
    String[] parts = credentials.getAccountName().split(":");

    logger.info(String.format("Creds : %s", String.join(", ", parts)));
    logger.info(String.format("Creds : endpoint %s", credentials.getEndpoint()));
    logger.info(String.format("Creds : pass %s", credentials.getAccountPass()));

    IOSClientBuilder.V3 builder = OSFactory.builderV3()
                .endpoint(credentials.getEndpoint())
                .credentials(parts[2], credentials.getAccountPass(), Identifier.byName(parts[0]))
                .scopeToProject(Identifier.byName(parts[1]), Identifier.byName(parts[0]));
 
    return builder;
  }

  public void authenticate()
      throws AuthenticationException {
    this.os = this.builder.authenticate();
    this.token = this.os.getToken();
  }

  public void reauth()
      throws AuthenticationException {
      // Re auth if token expired
    if (this.token.getExpires().getTime() > new Date().getTime()) {
      this.authenticate(); 
    }
    this.os = OSFactory.clientFromToken(this.token);
  }

  public OSClientV3 getOsClient() {
    this.reauth();
    return this.os;
  }
  
  public ComputeService getCompute() {
    this.reauth();
    return this.os.compute();
  }

  public NovaCredentials getNovaCredentials() {
    return this.novaCredentials;
  }


  /*
  public NovaApi getNovaApi() {
    return this.os.compute();
  }

  public SecurityGroupApi getSecurityGroupApi() {
    return this.os.networking();
  }

  public KeyPairApi getKeyPairApi() {
    return keyPairApi;
  }
  */
}
