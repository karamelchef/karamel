/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kamal
 */
public class IpConverter {

  public static void main(String[] args) {
    ipRange("192.168.0.1", "192.168.1.3");
  }
  
  public static List<String> ipRange(String srcIp, String destIp) {
    List<String> ips = new ArrayList<>();
    long src = ipToLong(srcIp);
    long dest = ipToLong(destIp);

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
  public static String toHex(String ipAddress) {
    return Long.toHexString(IpConverter.ipToLong(ipAddress));
  }

  /**
   * Convert an IP address to a number
   *
   * @param ipAddress Input IP address
   *
   * @return The IP address as a number
   */
  public static long ipToLong(String ipAddress) {
    long result = 0;
    String[] atoms = ipAddress.split("\\.");

    for (int i = 3; i >= 0; i--) {
      result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
    }

    return result & 0xFFFFFFFF;
  }

  public static String longToIp(long ip) {
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
