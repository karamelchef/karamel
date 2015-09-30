/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.util;

import se.kth.karamel.common.util.IpAddressUtil;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.kth.karamel.common.exception.IpAddressException;

/**
 *
 * @author kamal
 */
public class IpAddressUtilTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testValidateFormat() throws IpAddressException {
    IpAddressUtil.validateFormat("192.168.0.1");
    IpAddressUtil.validateFormat("192.168.01.255");
    IpAddressUtil.validateFormat("192.168.01.255 ");

    exception.expect(IpAddressException.class);
    IpAddressUtil.validateFormat("192.168");
    IpAddressUtil.validateFormat("192.168.1");
    IpAddressUtil.validateFormat("192.168.01.256.");
    IpAddressUtil.validateFormat("192.168.01.256");
  }

  @Test
  public void testIpRange() throws IpAddressException {
    List<String> ipRange = IpAddressUtil.ipRange("192.168.33.11");
    Assert.assertTrue(ipRange.size() == 1);
    Assert.assertTrue(ipRange.contains("192.168.33.11"));
    ipRange = IpAddressUtil.ipRange("192.168.33.11-192.168.33.13");
    Assert.assertTrue(ipRange.size() == 3);
    Assert.assertTrue(ipRange.contains("192.168.33.11"));
    Assert.assertTrue(ipRange.contains("192.168.33.12"));
    Assert.assertTrue(ipRange.contains("192.168.33.13"));
    ipRange = IpAddressUtil.ipRange("192.168.0.0-192.168.255.255");
    Assert.assertTrue(ipRange.size() == 65536);

    exception.expect(IpAddressException.class);
    IpAddressUtil.ipRange("192.168.33.11-192.168.33.11");
    IpAddressUtil.ipRange("192.168.33.13-192.168.33.11");
  }
}
