/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.experiment;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.backend.Experiment;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

/**
 *
 * @author kamal
 */
@Path("/experiment/load")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class LoadExperiment extends AbstractCall {

  public LoadExperiment(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @POST
  public Response loadExperiment(@FormParam("experimentUrl") String experimentUrl) {
    Response response = null;
    logger.debug(" Received request to set github credentials.... ");
    try {
      Experiment ec = karamelApi.loadExperiment(experimentUrl);
      response = Response.status(Response.Status.OK).entity(ec).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }

    return response;
  }
}
