package se.kth.karamel.backend.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;

public class ShellCommandBuilder {

  public static List<ShellCommand> makeSingleFileCommand(String filePath, String... pairs)
      throws IOException {
    String script = Resources.toString(Resources.getResource(filePath), Charsets.UTF_8);
    if (pairs.length > 0) {
      for (int i = 0; i < pairs.length; i += 2) {
        String key = pairs[i];
        String val = pairs[i + 1];
        script = script.replaceAll("%" + key + "%", val);
      }
    }
    List<ShellCommand> cmds = new ArrayList<>();
    ShellCommand shellCommand = new ShellCommand(script);
    cmds.add(shellCommand);
    return cmds;
  }

}
