/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.github;

import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.backend.github.OrgItem;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

/**
 *
 * @author kamal
 */
@Path("/github/getOrgs")
@Produces(MediaType.APPLICATION_JSON)
public class GetGithubOrgs extends AbstractCall {

  public GetGithubOrgs(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @POST
  public Response getGithubOrgs() {
    Response response = null;
    logger.debug(" Received request to set github credentials.... ");
    try {
      List<OrgItem> orgs = karamelApi.listGithubOrganizations();
      response = Response.status(Response.Status.OK).
          entity(orgs).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }

    return response;
  }
}
