/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.gce;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.GceJson;
import se.kth.karamel.webservicemodel.StatusResponseJSON;

/**
 *
 * @author kamal
 */
@Path("/gce/validateCredentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ValidateGceCredentials extends AbstractCall {

  private static final Logger logger = Logger.getLogger(ValidateGceCredentials.class);

  public ValidateGceCredentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response validateCredentials(GceJson providerJSON) {

    Response response = null;
    logger.debug("Received request to validate the gce credentials.");

    try {
      String jsonKeyPath = providerJSON.getJsonKeyPath();
      if (karamelApi.updateGceCredentialsIfValid(jsonKeyPath)) {
        response = Response.status(Response.Status.OK).
            entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
      } else {
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
            entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, "Invalid Credentials")).build();
      }

    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }
    return response;
  }
}
