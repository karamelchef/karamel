package se.kth.karamel.backend.launcher.nova;

import org.apache.log4j.Logger;
import org.jclouds.ContextBuilder;

import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.rest.AuthorizationException;
import se.kth.karamel.common.NovaCredentials;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;

/**
 * Created by Alberto on 2015-05-16.
 */
public final class NovaLauncher {

    private static final Logger logger = Logger.getLogger(NovaLauncher.class);
    public final NovaContext novaContext;
    public final SshKeyPair sshKeyPair;

    public NovaLauncher(NovaContext novaContext, SshKeyPair sshKeyPair) {
        this.novaContext = novaContext;
        this.sshKeyPair = sshKeyPair;
        logger.info(String.format("Account-id='%s'", novaContext.getNovaCredentials().getAccountName()));
        logger.info(String.format("Public-key='%s'", sshKeyPair.getPublicKeyPath()));
        logger.info(String.format("Private-key='%s'", sshKeyPair.getPrivateKeyPath()));
    }

    public static NovaContext validateCredentials(NovaCredentials novaCredentials,ContextBuilder builder) throws InvalidNovaCredentialsException {
        try {
            NovaContext context = new NovaContext(novaCredentials, builder);
            SecurityGroupApi securityGroupApi = context.getSecurityGroupApi();
            securityGroupApi.list();
            return context;
        }catch (AuthorizationException e){
            throw new InvalidNovaCredentialsException("account-name:" + novaCredentials.getAccountName(),e);
        }
    }
}
