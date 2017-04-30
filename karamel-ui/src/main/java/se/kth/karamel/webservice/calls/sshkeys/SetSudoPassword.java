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
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.StatusResponseJSON;
import se.kth.karamel.webservicemodel.SudoPasswordJSON;

/**
 *
 * @author kamal
 */
@Path("/ssh/setSudoPassword")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SetSudoPassword extends AbstractCall {

  public SetSudoPassword(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response sudoPassword(SudoPasswordJSON sudoPwd) {
    Response response = null;
    logger.debug(" Received request to set sudo password....");
    try {
      karamelApi.registerSudoPassword(sudoPwd.getPassword());
      response = Response.status(Response.Status.OK).
          entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }
    return response;
  }
}
