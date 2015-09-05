/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.github;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.backend.github.RepoItem;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

/**
 *
 * @author kamal
 */
@Path("/github/getRepos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class GetGithubRepos extends AbstractCall {

  public GetGithubRepos(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @POST
  public Response getGithubRepos(@FormParam("org") String org) {
    Response response = null;
    logger.info(" Received request to set github credentials.... ");
    try {
      List<RepoItem> repos = karamelApi.listGithubRepos(org);
      response = Response.status(Response.Status.OK).
          entity(repos).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }

    return response;
  }
}
