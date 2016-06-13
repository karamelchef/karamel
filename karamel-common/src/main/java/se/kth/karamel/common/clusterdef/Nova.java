package se.kth.karamel.common.clusterdef;

import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.launcher.nova.NovaSetting;

/**
 * Created by alberto on 2015-05-14.
 */
public class Nova extends Provider {

  private String flavor;
  private String image;

  public static Nova makeDefault() {
    Nova nova = new Nova();
    return nova.applyDefaults();
  }

  public String getFlavor() {
    return flavor;
  }

  public void setFlavor(String flavor) {
    this.flavor = flavor;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  @Override
  public Nova cloneMe() {
    Nova nova = new Nova();
    nova.setUsername(getUsername());
    nova.setImage(image);
    nova.setFlavor(flavor);
    return nova;
  }

  @Override
  public Nova applyParentScope(Provider parentScopeProvider) {
    Nova clone = cloneMe();
    if (parentScopeProvider instanceof Nova) {
      Nova parentNova = (Nova) parentScopeProvider;
      if (clone.getUsername() == null) {
        clone.setUsername(parentNova.getUsername());
      }
      if (clone.getImage() == null) {
        clone.setImage(parentNova.getImage());
      }
    }
    return clone;
  }

  @Override
  public Nova applyDefaults() {
    Nova clone = cloneMe();
    //TODO add default settings for openstack
    if (clone.getUsername() == null) {
      clone.setUsername(NovaSetting.NOVA_DEFAULT_USERNAME.getParameter());
    }
    if (clone.getImage() == null) {
      clone.setImage(NovaSetting.NOVA_DEFAULT_IMAGE.getParameter());
    }
    if (clone.getFlavor() == null) {
      clone.setFlavor(NovaSetting.NOVA_DEFAULT_FLAVOR.getParameter());
    }
    return clone;
  }

  @Override
  public void validate() throws ValidationException {
    //TODO validation exception to think of
  }
}
