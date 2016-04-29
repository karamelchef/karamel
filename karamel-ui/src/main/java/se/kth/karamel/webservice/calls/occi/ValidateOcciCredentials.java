package se.kth.karamel.webservice.calls.occi;

import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.OcciCredentials;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.OcciJSON;
import se.kth.karamel.webservicemodel.StatusResponseJSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Mamut on 2016-01-20.
 */

@Path("/occi/validateCredentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ValidateOcciCredentials extends AbstractCall {

  private static final Logger logger = Logger.getLogger(ValidateOcciCredentials.class);

  public ValidateOcciCredentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response validateCredentials(OcciJSON providerJSON) {

    Response response = null;
    logger.debug(" Received request to validate the occi credentials.");

    try {
      OcciCredentials credentials = new OcciCredentials();
      credentials.setUserCertificatePath(providerJSON.getUserCertificatePath());
      credentials.setSystemCertDir(providerJSON.getSystemCertDir());
      if (karamelApi.updateOcciCredentialsIfValid(credentials)) {
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
