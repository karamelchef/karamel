package se.kth.karamel.common.util.settings;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 2015-05-16.
 */
public enum SystemSetting {

  USER_HOME(System.getProperty("user.home")),
  USER_NAME(System.getProperty("user.name")),
  OS_NAME(System.getProperty("os.name")),
  IP_Address(loadIpAddress()),
  DEFAULT_PUBKEY_PATH(checkUnixOS() ? USER_HOME + "/.ssh/id_rsa.pub" : null),
  DEFAULT_PRIKEY_PATH(checkUnixOS() ? USER_HOME + "/.ssh/id_rsa" : null),
  SSH_PUBKEY_PATH_KEY("ssh.publickey.path"),
  SSH_PRIVKEY_PATH_KEY("ssh.privatekey.path"),
  SSH_PRIVKEY_PASSPHRASE("ssh.privatekey.passphrase");
  private static final Map<String, SystemSetting> lookup
          = new HashMap<String, SystemSetting>();

  static {
    for (SystemSetting s : EnumSet.allOf(SystemSetting.class))
      lookup.put(s.getParameter(), s);
  }

  private String parameter;

  private SystemSetting(String parameter) {
    this.parameter = parameter;
  }

  public static SystemSetting get(String parameter) {
    return lookup.get(parameter);
  }

  public static String loadIpAddress() {
    String address = "UnknownHost";
    try {
      address = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException ex) {
    }
    return address;
  }

  public static boolean checkUnixOS() {
    return OS_NAME.getParameter().toLowerCase().contains("mac") ||
            OS_NAME.getParameter().toLowerCase().contains("linux");
  }

  public String getParameter() {
    return parameter;
  }
}
