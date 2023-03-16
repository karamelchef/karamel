/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;

import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.common.util.IoUtils;

/**
 *
 * @author kamal
 */
public class ShellCommandBuilder {

  public static List<ShellCommand> fileScript2LinebyLineCommands(String filePath, String... pairs)
      throws IOException {
    String script = IoUtils.readContentFromClasspath(filePath);
    if (pairs.length > 0) {
      for (int i = 0; i < pairs.length; i += 2) {
        String key = pairs[i];
        String val = pairs[i + 1];
        script = script.replaceAll("%" + key + "%", val);
      }
    }
    List<ShellCommand> cmds = makeLineByLineCommands(script);
    return cmds;
  }

  public static List<ShellCommand> makeLineByLineCommands(String script) throws IOException {
    List<ShellCommand> tasks = new ArrayList<>();
    Scanner scanner = new Scanner(script);
    while (scanner.hasNextLine()) {
      String nextCmd = scanner.nextLine();
      if (nextCmd.contains("cat") && nextCmd.contains("END_OF_FILE")) {
        StringBuilder cmdBuf = new StringBuilder();
        cmdBuf.append(nextCmd);
        String newLine = null;
        do {
          newLine = scanner.nextLine();
          cmdBuf.append("\n").append(newLine);
        } while (!newLine.contains("END_OF_FILE"));
        tasks.add(new ShellCommand(cmdBuf.toString()));
      } else {
        tasks.add(new ShellCommand(nextCmd));
      }
    }
    return tasks;
  }

  public static List<ShellCommand> makeSingleFileCommand(String filePath, String... pairs)
      throws IOException {
    String script = IoUtils.readContentFromClasspath(filePath);
    if (pairs.length > 0) {
      for (int i = 0; i < pairs.length; i += 2) {
        String key = pairs[i];
        String val = pairs[i + 1];
        if (key == null || val == null) {
          continue;
        }
        script = script.replaceAll("%" + key + "%", Matcher.quoteReplacement(val));
      }
    }
    List<ShellCommand> cmds = new ArrayList<>();
    ShellCommand shellCommand = new ShellCommand(script);
    cmds.add(shellCommand);
    return cmds;
  }

}
