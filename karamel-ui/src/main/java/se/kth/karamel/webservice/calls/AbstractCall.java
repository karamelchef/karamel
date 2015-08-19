/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls;

import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.webservicemodel.StatusResponseJSON;

/**
 *
 * @author kamal
 */
public abstract class AbstractCall {

  protected static final Logger logger = Logger.getLogger(AbstractCall.class);
  protected KaramelApi karamelApi;
  
  public AbstractCall(KaramelApi karamelApi) {
    this.karamelApi = karamelApi;
  }
  
  protected Response buildExceptionResponse(Exception e) {
    logger.error("", e);
    Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
        entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
    return response;
  }
}
