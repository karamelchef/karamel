/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.cluster;

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
import se.kth.karamel.webservicemodel.KaramelBoardJSON;
import se.kth.karamel.webservicemodel.StatusResponseJSON;

/**
 *
 * @author kamal
 */
@Path("/startCluster")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StartCluster extends AbstractCall {

  private static final Logger logger = Logger.getLogger(StartCluster.class);

  public StartCluster(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response startCluster(KaramelBoardJSON boardJSON) {

    Response response = null;
    logger.debug("Start cluster: " + System.lineSeparator() + boardJSON.getJson());

    try {
      karamelApi.startCluster(boardJSON.getJson());
      response = Response.status(Response.Status.OK).
          entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }
    
    return response;
  }
}
