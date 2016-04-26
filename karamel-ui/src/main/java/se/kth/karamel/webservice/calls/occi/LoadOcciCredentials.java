package se.kth.karamel.webservice.calls.occi;

import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.OcciJSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.OcciCredentials;

/**
 * Created by Mamut on 2015-1-18.
 */
@Path("/occi/loadCredentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoadOcciCredentials extends AbstractCall {

  private static final Logger logger = Logger.getLogger(LoadOcciCredentials.class);

  public LoadOcciCredentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response loadCredentials() {
    Response response = null;
    logger.debug("Received request to load the occi credentials.");
    try {
      OcciCredentials credentials = karamelApi.loadOcciCredentialsIfExist();
      OcciJSON provider = new OcciJSON();
      provider.setUserCertificatePath((credentials == null) ? "" : credentials.getUserCertificatePath());
      provider.setSystemCertDir((credentials == null) ? "" : credentials.getSystemCertDir());

      response = Response.status(Response.Status.OK).entity(provider).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);;
    }

    return response;
  }
}
