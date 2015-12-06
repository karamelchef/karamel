/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.ec2;

import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.Ec2JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author kamal
 */
@Path("/ec2/loadCredentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoadEc2Credentials extends AbstractCall {

  private static final Logger logger = Logger.getLogger(LoadEc2Credentials.class);

  public LoadEc2Credentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response loadCredentials() {
    Response response = null;
    logger.debug("Received request to load the ec2 credentials.");
    try {
      Ec2Credentials credentials = karamelApi.loadEc2CredentialsIfExist();
      Ec2JSON provider = new Ec2JSON();
      provider.setAccountId((credentials == null) ? "" : credentials.getAccessKey());
      provider.setAccountKey((credentials == null) ? "" : credentials.getSecretKey());
      response = Response.status(Response.Status.OK).entity(provider).build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }
    return response;
  }
}
