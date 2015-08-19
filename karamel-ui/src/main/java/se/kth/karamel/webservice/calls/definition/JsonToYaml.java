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
import se.kth.karamel.webservicemodel.KaramelBoardJSON;
import se.kth.karamel.webservicemodel.KaramelBoardYaml;

/**
 *
 * @author kamal
 */
@Path("/definition/json2yaml")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonToYaml extends AbstractCall {

  public JsonToYaml(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response getYamlForJSON(KaramelBoardJSON karamelBoardJSON) {
    Response response = null;
    logger.debug("Fetch Yaml Called ... ");

    try {
      String yml = karamelApi.jsonToYaml(karamelBoardJSON.getJson());
      KaramelBoardYaml karamelBoardYaml = new KaramelBoardYaml(yml);
      response = Response.status(Response.Status.OK).entity(karamelBoardYaml).build();

    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }
    return response;
  }
}
