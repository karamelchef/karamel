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

/**
 *
 * @author kamal
 */
@Path("/gce/loadCredentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoadGceCredentials extends AbstractCall {

  private static final Logger logger = Logger.getLogger(LoadGceCredentials.class);

  public LoadGceCredentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response loadCredentials() {
    Response response = null;
    logger.debug("Received request to load the gce credentials.");
    try {
      String jsonKeyPath = karamelApi.loadGceCredentialsIfExist();
      GceJson provider = new GceJson();
      provider.setJsonKeyPath((jsonKeyPath == null) ? "" : jsonKeyPath);
      response = Response.status(Response.Status.OK).entity(provider).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }
    return response;
  }

}
