/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservice.calls.cluster;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.CommandJSON;

/**
 *
 * @author kamal
 */
@Path("/cluster/processCommand")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProcessCommand extends AbstractCall {

  public ProcessCommand(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response processCommand(CommandJSON command) {
    Response response = null;

    logger.debug("Received request to process a command with info: " + command.getCommand());
    try {
      CommandResponse cmdRes = karamelApi.processCommand(command.getCommand(), command.getResult());
      command.setResult(cmdRes.getResult());
      command.setNextCmd(cmdRes.getNextCmd());
      command.setRenderer(cmdRes.getRenderer().name().toLowerCase());
      command.getMenuItems().addAll(cmdRes.getMenuItems());
      command.setSuccessmsg(cmdRes.getSuccessMessage());
      command.setContext(cmdRes.getContext());
    } catch (KaramelException e) {
      command.setErrormsg(e.getMessage());
    } catch (Exception e) {
      command.setErrormsg(e.getMessage());
    } finally {
      response = Response.status(Response.Status.OK).entity(command).build();
    }
    return response;
  }
}
