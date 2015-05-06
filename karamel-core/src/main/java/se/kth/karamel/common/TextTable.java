/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kamal
 */
public class TextTable {

  public static String makeTable(String[] columnNames, int sortIndex, Object[][] data, boolean rowNumbering) {
    StringBuilder builder = new StringBuilder();
    int[] colLengths = calcColLengths(columnNames, data);
    int allColsLen = 0;
    for (int i : colLengths) {
      allColsLen += i;
    }
    int totalLegth = columnNames.length * 1 //column marginal space
            + (columnNames.length + 1) //pipe lines 
            + allColsLen;  //real data size

    int paddingLen = 0;
    if (rowNumbering && data.length > 0) {
      int len = data.length;
      while (len > 0) {
        paddingLen++;
        len /= 10;
      }
    }

    if (rowNumbering) {
      builder.append(StringUtils.repeat(" ", paddingLen + 2));
    }

    //top line _______
    builder.append(StringUtils.repeat("_", totalLegth));

    builder.append("\n");

    if (rowNumbering) {
      builder.append(StringUtils.repeat(" ", paddingLen + 2));
    }

    //title row
    for (int i = 0; i < columnNames.length; i++) {
      builder.append(String.format("| %-" + colLengths[i] + "s", columnNames[i]));
    }

    builder.append("|"); //closing pipe of the title line

    builder.append("\n");
    //botton line =====
    if (rowNumbering) {
      builder.append(StringUtils.repeat(" ", paddingLen + 2));
    }
    builder.append(StringUtils.repeat("=", totalLegth));
    //data rows
    for (int x = 0; x < data.length; x++) {
      Object[] row = data[x];
      builder.append("\n");
      if (rowNumbering) {
        builder.append(String.format("%" + paddingLen + "d. ", x + 1));
      }
      for (int i = 0; i < row.length; i++) {
        builder.append(String.format("| %-" + colLengths[i] + "s", row[i]));
      }

      builder.append("|"); //closing pipe of the row line
    }
    return builder.toString();
  }

  private static int[] calcColLengths(String[] columnNames, Object[][] data) {
    int[] columnLengths = new int[columnNames.length];

    for (int i = 0; i < columnNames.length; i++) {
      String title = columnNames[i];
      int maxLength = 0;
      maxLength = maxLength(maxLength, title);
      for (Object[] data1 : data) {
        Object cell = data1[i];
        maxLength = maxLength(maxLength, cell);
      }
      columnLengths[i] = maxLength;
    }
    return columnLengths;
  }

  private static int maxLength(int length, Object data) {
    int l = (data == null) ? 0 : realDataLen(data);
    return Math.max(l, length);
  }

  private static Pattern METADATA_PATTERN = Pattern.compile("(<a[^>]*>([^<>]*)<\\/a>)");

  public static int realDataLen(Object data) {
    if (data == null) {
      return 0;
    }
    String dataString = data.toString();
    int totalLength = dataString.length();
    Matcher matcher = METADATA_PATTERN.matcher(dataString);
    while (matcher.find()) {
      String withMeta = matcher.group(1);
      String justData = matcher.group(2);
      int metaLegth = withMeta.length() - justData.length();
      totalLength -= metaLegth;
    }
    return totalLength;
  }

}
