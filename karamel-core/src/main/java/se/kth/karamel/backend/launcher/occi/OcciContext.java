package se.kth.karamel.backend.launcher.occi;

import se.kth.karamel.common.util.OcciCredentials;


/**
 * Occi Context
 * Created by Mamut on 2015-05-16.
 */
public class OcciContext {
  private final OcciCredentials occiCredentials;

  public OcciContext(OcciCredentials credentials) {
    this.occiCredentials = credentials;
  }

  public OcciCredentials getOcciCredentials() {
    return occiCredentials;
  }
}
