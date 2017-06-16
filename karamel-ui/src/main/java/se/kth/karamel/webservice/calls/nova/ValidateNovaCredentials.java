package se.kth.karamel.webservice.calls.nova;

import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.NovaCredentials;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.NovaJSON;
import se.kth.karamel.webservicemodel.StatusResponseJSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by alberto on 12/6/15.
 */

@Path("/nova/validateCredentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ValidateNovaCredentials extends AbstractCall {

  private static final Logger logger = Logger.getLogger(ValidateNovaCredentials.class);

  public ValidateNovaCredentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response validateCredentials(NovaJSON providerJSON) {

    Response response = null;
    logger.debug(" Received request to validate the nova credentials.");

    try {
      NovaCredentials credentials = new NovaCredentials();
      credentials.setAccountName(providerJSON.getAccountName());
      credentials.setAccountPass(providerJSON.getAccountPass());
      credentials.setEndpoint(providerJSON.getEndpoint());
      credentials.setRegion(providerJSON.getRegion());
      credentials.setVersion(providerJSON.getVersion());
      credentials.setNetworkId(providerJSON.getNetworkId());
      if (karamelApi.updateNovaCredentialsIfValid(credentials)) {
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
