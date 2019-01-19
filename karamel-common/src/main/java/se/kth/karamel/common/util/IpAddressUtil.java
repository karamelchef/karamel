/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.common.exception.IpAddressException;

/**
 *
 * @author kamal
 */
public class IpAddressUtil {

  public static Pattern IP_PATTERN = Pattern.compile(Settings.IP_REGEX);

  public static List<String> ipRange(String ipStr) throws IpAddressException {
    if (ipStr.contains("-")) {
      String[] indivIp = ipStr.split("-");
      if (indivIp.length != 2) {
        throw new IpAddressException(String.format("Ip range format is invalid '%s'", ipStr));
      }
      return ipRange(indivIp[0], indivIp[1]);
    } else {
      validateFormat(ipStr);
      ArrayList<String> ips = new ArrayList<>();
      ips.add(ipStr.trim());
      return ips;
    }
  }

  public static void validateFormat(String ip) throws IpAddressException {
    if (!ip.trim().matches(Settings.IP_REGEX)) {
      throw new IpAddressException("Ip format is invalid " + ip);
    }
  }

  public static List<String> ipRange(String srcIp, String destIp) throws IpAddressException {
    validateFormat(srcIp);
    validateFormat(destIp);
    List<String> ips = new ArrayList<>();
    long src = ipToLong(srcIp.trim());
    long dest = ipToLong(destIp.trim());

    if (dest < src) {
      throw new IpAddressException(String.format("start ip is greater than the end %s-%s", srcIp, destIp));
    }
    long count = src;
    while (count <= dest) {
      ips.add(longToIp(count));
      count++;
    }
    return ips;
  }

  /**
   * Convert an IP address to a hex string
   *
   * @param ipAddress Input IP address
   *
   * @return The IP address in hex form
   */
  protected static String toHex(String ipAddress) throws IpAddressException {
    return Long.toHexString(IpAddressUtil.ipToLong(ipAddress));
  }

  /**
   * Convert an IP address to a number
   *
   * @param ipAddress Input IP address
   *
   * @return The IP address as a number
   */
  protected static long ipToLong(String ipAddress) throws IpAddressException {
    long result = 0;
    Matcher m = IP_PATTERN.matcher(ipAddress);
    if (m.matches()) {
      for (int i = 3; i >= 0; i--) {
        result |= (Long.parseLong(m.group(4 - i)) << (i * 8));
      }
    } else {
      throw new IpAddressException("Invalid ip format " + ipAddress);
    }
    return result & 0xFFFFFFFF;
  }

  protected static String longToIp(long ip) {
    StringBuilder sb = new StringBuilder(15);

    for (int i = 0; i < 4; i++) {
      sb.insert(0, Long.toString(ip & 0xff));

      if (i < 3) {
        sb.insert(0, '.');
      }

      ip >>= 8;
    }

    return sb.toString();
  }
}
