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
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

/**
 *
 * @author kamal
 */
@Path("/ssh/generateKey")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GenerateSshKeys extends AbstractCall {

  public GenerateSshKeys(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response generate() {
    Response response = null;
    logger.debug("Received request to generate ssh keys.");
    try {
      SshKeyPair sshKeypair = karamelApi.generateSshKeysAndUpdateConf();
      karamelApi.registerSshKeys(sshKeypair);
      response = Response.status(Response.Status.OK).entity(sshKeypair).build();
    } catch (KaramelException ex) {
      response = buildExceptionResponse(ex);
    }
    return response;
  }

}
