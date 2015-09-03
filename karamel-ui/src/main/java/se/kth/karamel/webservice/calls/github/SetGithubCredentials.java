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
import se.kth.karamel.backend.github.GithubUser;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

/**
 *
 * @author kamal
 */
@Path("/github/setCredentials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class SetGithubCredentials extends AbstractCall {

  public SetGithubCredentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @POST
  public Response setGithubCredentials(@FormParam("user") String user, @FormParam("password") String password) {
    Response response = null;
    logger.info(" Received request to set github credentials.... ");
    try {
      GithubUser githubUser = karamelApi.registerGithubAccount(user, password);

      response = Response.status(Response.Status.OK).
          entity(githubUser).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }

    return response;
  }
}
