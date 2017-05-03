/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.github;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.backend.github.GithubUser;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

/**
 *
 * @author kamal
 */
@Path("/github/getCredentials")
@Produces(MediaType.APPLICATION_JSON)
public class GetGithubCredentials extends AbstractCall {

  public GetGithubCredentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @GET
  public Response getGithubCredentials() {
    Response response = null;
    logger.debug(" Received request to get github credentials.... ");
    try {
      GithubUser credentials = karamelApi.loadGithubCredentials();
      response = Response.status(Response.Status.OK).
          entity(credentials).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }
    return response;
  }
}
