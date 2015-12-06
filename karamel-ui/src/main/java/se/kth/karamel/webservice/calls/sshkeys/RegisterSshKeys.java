/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.sshkeys;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.SshKeyJSON;

/**
 *
 * @author kamal
 */
@Path("/ssh/registerKey")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegisterSshKeys extends AbstractCall {

  public RegisterSshKeys(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response register(SshKeyJSON sshKeysJSON) {
    Response response = null;
    logger.debug("Received request to register ssh keys.");
    SshKeyPair sshKeypair = new SshKeyPair();
    sshKeypair.setPublicKeyPath(sshKeysJSON.getPubKeyPath());
    sshKeypair.setPrivateKeyPath(sshKeysJSON.getPrivKeyPath());
    sshKeypair.setPassphrase(sshKeysJSON.getPassphrase());
    try {
      karamelApi.registerSshKeys(sshKeypair);
      response = Response.status(Response.Status.OK).entity(sshKeypair).build();
    } catch (KaramelException ex) {
      response = buildExceptionResponse(ex);
    }

    return response;
  }
}
