/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.launcher.amazon;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import static org.jclouds.Constants.PROPERTY_CONNECTION_TIMEOUT;
import org.jclouds.ContextBuilder;
import org.jclouds.aws.ec2.features.AWSKeyPairApi;
import static org.jclouds.aws.ec2.reference.AWSEC2Constants.PROPERTY_EC2_AMI_QUERY;
import static org.jclouds.aws.ec2.reference.AWSEC2Constants.PROPERTY_EC2_CC_AMI_QUERY;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_PORT_OPEN;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE;
import org.jclouds.ec2.EC2Api;
import org.jclouds.ec2.features.SecurityGroupApi;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;

/**
 *
 * @author kamal
 */
public class Ec2Context {

  private final String accountId;
  private final String accessKey;
  private final ComputeService computeService;
  private final EC2Api ec2api;
  private final SecurityGroupApi securityGroupApi;
  private final AWSKeyPairApi keypairApi;

  public Ec2Context(String accountId, String accessKey) {
    this.accountId = accountId;
    this.accessKey = accessKey;
    Properties properties = new Properties();
    long scriptTimeout = TimeUnit.MILLISECONDS.convert(50, TimeUnit.MINUTES);
    properties.setProperty(TIMEOUT_SCRIPT_COMPLETE, scriptTimeout + "");
    properties.setProperty(TIMEOUT_PORT_OPEN, scriptTimeout + "");
    properties.setProperty(PROPERTY_CONNECTION_TIMEOUT, scriptTimeout + "");
    properties.setProperty(PROPERTY_EC2_AMI_QUERY, "owner-id=137112412989;state=available;image-type=machine");
    properties.setProperty(PROPERTY_EC2_CC_AMI_QUERY, "");
    Iterable<Module> modules = ImmutableSet.<Module>of(
            new SshjSshClientModule(),
            new SLF4JLoggingModule(),
            new EnterpriseConfigurationModule());

    ContextBuilder build = ContextBuilder.newBuilder("aws-ec2")
            .credentials(accountId, accessKey)
            .modules(modules)
            .overrides(properties);
    ComputeServiceContext context = build.buildView(ComputeServiceContext.class);
    this.computeService = context.getComputeService();
    this.ec2api = computeService.getContext().unwrapApi(EC2Api.class);
    this.securityGroupApi = ec2api.getSecurityGroupApi().get();
    this.keypairApi = (AWSKeyPairApi) ec2api.getKeyPairApi().get();
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getAccountId() {
    return accountId;
  }

  public ComputeService getComputeService() {
    return computeService;
  }

  public EC2Api getEc2api() {
    return ec2api;
  }

  public AWSKeyPairApi getKeypairApi() {
    return keypairApi;
  }

  public SecurityGroupApi getSecurityGroupApi() {
    return securityGroupApi;
  }
  
}
