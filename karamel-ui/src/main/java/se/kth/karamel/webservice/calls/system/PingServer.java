/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.system;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.webservice.calls.AbstractCall;

/**
 *
 * @author kamal
 */
@Path("/system/ping")
public class PingServer extends AbstractCall {

  public PingServer(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @GET
  public Response pingKaramel() {
    Response response = Response.status(Response.Status.OK).build();
    return response;
  }

}
