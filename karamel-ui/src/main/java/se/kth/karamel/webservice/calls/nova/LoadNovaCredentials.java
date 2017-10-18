package se.kth.karamel.webservice.calls.nova;

import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.NovaCredentials;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.NovaJSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by alberto on 12/6/15.
 */
@Path("/nova/loadCredentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoadNovaCredentials extends AbstractCall {

  private static final Logger logger = Logger.getLogger(LoadNovaCredentials.class);

  public LoadNovaCredentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response loadCredentials() {
    Response response = null;
    logger.debug("Received request to load the nova credentials.");
    try {
      NovaCredentials credentials = karamelApi.loadNovaCredentialsIfExist();
      NovaJSON provider = new NovaJSON();
      provider.setAccountName((credentials == null) ? "" : credentials.getAccountName());
      provider.setAccountPass((credentials == null) ? "" : credentials.getAccountPass());
      provider.setVersion((credentials == null) ? "v2" : credentials.getVersion());
      provider.setEndpoint((credentials == null) ? "" : credentials.getEndpoint());
      provider.setRegion((credentials == null) ? "" : credentials.getRegion());
      provider.setNetworkId((credentials == null) ? "" : credentials.getNetworkId());
      
      response = Response.status(Response.Status.OK).entity(provider).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);;
    }
    return response;
  }
}
