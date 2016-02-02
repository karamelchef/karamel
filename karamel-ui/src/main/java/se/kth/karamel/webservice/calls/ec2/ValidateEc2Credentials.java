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
import se.kth.karamel.webservicemodel.StatusResponseJSON;

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
@Path("/ec2/validateCredentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ValidateEc2Credentials extends AbstractCall {

  private static final Logger logger = Logger.getLogger(ValidateEc2Credentials.class);

  public ValidateEc2Credentials(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response validateCredentials(Ec2JSON providerJSON) {

    Response response = null;
    logger.debug("Received request to validate the ec2 credentials.");

    try {
      Ec2Credentials credentials = new Ec2Credentials();
      credentials.setAccessKey(providerJSON.getAccountId());
      credentials.setSecretKey(providerJSON.getAccountKey());
      if (karamelApi.updateEc2CredentialsIfValid(credentials)) {
        response = Response.status(Response.Status.OK).
            entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
      } else {
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
            entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, "Invalid Credentials")).build();
      }

    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }
    return response;
  }

}
