/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.commad;

import static org.junit.Assert.*;
import org.junit.Test;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.backend.command.CommandService;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class CommandServiceTest {

  @Test
  public void testCommands() throws KaramelException {
    CommandResponse commandResponse = CommandService.processCommand("home");
    assertNotNull(commandResponse);
    assertNotNull(commandResponse.getResult());
    assertEquals(CommandResponse.Renderer.INFO, commandResponse.getRenderer());
    commandResponse = CommandService.processCommand("help");
    assertNotNull(commandResponse);
    assertNotNull(commandResponse.getResult());
    assertEquals(CommandResponse.Renderer.INFO, commandResponse.getRenderer());
    try {
      CommandService.processCommand("yaml");
      CommandService.processCommand("yaml hadoop");
    } catch (KaramelException e) {

    }

  }
}
