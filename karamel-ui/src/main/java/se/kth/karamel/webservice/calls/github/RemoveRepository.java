/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.github;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.StatusResponseJSON;

/**
 *
 * @author kamal
 */
@Path("/github/removeRepository")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class RemoveRepository extends AbstractCall {

  public RemoveRepository(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @POST
  public Response removeRepository(@FormParam("org") String org, @FormParam("repo") String repo,
      @FormParam("local") boolean local, @FormParam("remote") boolean remote) {
    Response response = null;
    logger.info(" Received request to set github credentials.... ");
    try {
      karamelApi.removeRepo(org, repo, local, remote);
      response = Response.status(Response.Status.OK).
          entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }
    return response;
  }

}
