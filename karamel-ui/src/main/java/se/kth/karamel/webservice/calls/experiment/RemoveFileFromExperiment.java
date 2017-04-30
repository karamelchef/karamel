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
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.StatusResponseJSON;

/**
 *
 * @author kamal
 */
@Path("/experiment/removeFile")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class RemoveFileFromExperiment extends AbstractCall {

  public RemoveFileFromExperiment(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @POST
  public Response removeFileFromExperiment(@FormParam("org") String org, @FormParam("repo") String repo,
      @FormParam("filename") String filename) {
    Response response = null;
    try {
      logger.debug(" Received request to set github credentials.... ");
      karamelApi.removeFileFromExperiment(org, repo, filename);
      response = Response.status(Response.Status.OK).entity(
          new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
    } catch (Exception e) {
      response = buildExceptionResponse(e);
    }
    return response;
  }

}
