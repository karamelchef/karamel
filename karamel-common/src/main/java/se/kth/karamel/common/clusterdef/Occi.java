package se.kth.karamel.common.clusterdef;

import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.util.Settings;

/**
 * Created by Mamut3D on 2016-1-18.
 */
public class Occi extends Provider {

  private String occiEndpoint;
  private String occiImage;  
  private String occiImageSize;  

  public static Occi makeDefault() {
    Occi occi = new Occi();
    return occi.applyDefaults();
  }
 
  public String getOcciEndpoint() {
    return occiEndpoint;
  }

  public void setOcciEndpoint(String occiEndpoint) {
    this.occiEndpoint = occiEndpoint;
  }
  
  public String getOcciImage() {
    return occiImage;
  }

  public void setOcciImage(String occiImage) {
    this.occiImage = occiImage;
  } 
  
  public String getOcciImageSize() {
    return occiImageSize;
  }

  public void setOcciImageSize(String occiImageSize) {
    this.occiImageSize = occiImageSize;
  } 
  
  
  @Override
  public Occi cloneMe() {
    Occi occi = new Occi();
    occi.setUsername(getUsername());
    occi.setOcciEndpoint(occiEndpoint);
    occi.setOcciImage(occiImage);  
    occi.setOcciImageSize(occiImageSize); 
    return occi;
  }

  
  //TODO figure out what is this good for and finish it
  @Override
  public Occi applyParentScope(Provider parentScopeProvider) {
    Occi clone = cloneMe();
    if (parentScopeProvider instanceof Occi) {
      Occi parentOcci = (Occi) parentScopeProvider;
      if (clone.getUsername() == null) {
        clone.setUsername(parentOcci.getUsername());
      }    
      if (clone.getOcciEndpoint() == null) {
        clone.setOcciEndpoint(parentOcci.getOcciEndpoint());
      }
      if (clone.getOcciImage() == null) {
        clone.setOcciImage(parentOcci.getOcciImage());
      }
      if (clone.getOcciImageSize() == null) {
        clone.setOcciImageSize(parentOcci.getOcciImageSize());
      }
    }
    return clone;
  }

  @Override
  public Occi applyDefaults() {
    Occi clone = cloneMe();
    if (clone.getUsername() == null) {
      clone.setUsername(Settings.OCCI_DEFAULT_USERNAME);
    }
    if (clone.getOcciImage() == null) {
      clone.setOcciImage(Settings.OCCI_DEFAULT_IMAGE);
    }
    if (clone.getOcciImageSize() == null) {
      clone.setOcciImageSize(Settings.OCCI_DEFAULT_IMAGE_SIZE);
    }
    if (clone.getOcciEndpoint() == null) {
      clone.setOcciEndpoint(Settings.OCCI_DEFAULT_ENDPOINT);
    }  
    return clone;
  }

  @Override
  public void validate() throws ValidationException {
    //TODO validation exception to think of
  }
}
