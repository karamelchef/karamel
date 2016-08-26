/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author kamal
 */
public class StringUtils {

  public static List<String> toLines(String string) {
    List<String> lines = new ArrayList<>();
    StringReader reader = new StringReader(string);
    Scanner scanner = new Scanner(reader);
    List<String> comments = new ArrayList<>();
    while (scanner.hasNextLine()) {
      boolean found = false;
      String line = scanner.nextLine();
      lines.add(line);
    }
    return lines;
  }
}
