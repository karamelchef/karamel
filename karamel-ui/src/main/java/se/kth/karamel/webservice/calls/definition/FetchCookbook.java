/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.definition;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.CookbookJSON;

/**
 *
 * @author kamal
 */
@Path("/definition/fetchCookbook")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FetchCookbook extends AbstractCall {

  public FetchCookbook(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response getCookbook(CookbookJSON cookbookJSON) {
    Response response = null;
    try {
      String cookbookDetails = karamelApi.getCookbookDetails(cookbookJSON.getUrl(), cookbookJSON.isRefresh());
      response = Response.status(Response.Status.OK).entity(cookbookDetails).build();

    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }

    return response;
  }

}
