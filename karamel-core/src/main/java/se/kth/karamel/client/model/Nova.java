package se.kth.karamel.client.model;

import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.settings.NovaSetting;

/**
 * Created by alberto on 2015-05-14.
 */
public class Nova extends Provider{

    private String flavor;
    private String region;
    private String image;
    private String endpoint;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public static Nova makeDefault(){
        Nova nova = new Nova();
        return nova.applyDefaults();
    }

    @Override
    public Nova cloneMe() {
        Nova nova = new Nova();
        nova.setUsername(getUsername());
        nova.setImage(image);
        nova.setFlavor(flavor);
        nova.setRegion(region);
        nova.setEndpoint(endpoint);
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
            if (clone.getRegion() == null) {
                clone.setRegion(parentNova.getRegion());
            }
            if (clone.getEndpoint() == null) {
                clone.setEndpoint(parentNova.getEndpoint());
            }
        }
        return clone;
    }

    @Override
    public Nova applyDefaults() {
        Nova clone = cloneMe();
        //TODO add default settings for openstack
        if(clone.getUsername() == null){
            clone.setUsername(NovaSetting.OPENSTACK_NOVA_DEFAULT_USERNAME.getParameter());
        }
        if(clone.getImage() == null){
            clone.setImage(NovaSetting.OPENSTACK_NOVA_DEFAULT_IMAGE.getParameter());
        }
        if(clone.getFlavor() == null){
            clone.setFlavor(NovaSetting.OPENSTACK_NOVA_DEFAULT_FLAVOR.getParameter());
        }
        if(clone.getRegion() == null){
            clone.setImage(NovaSetting.OPENSTACK_NOVA_DEFAULT_REGION.getParameter());
        }
        if(clone.getEndpoint() == null){
            clone.setImage(NovaSetting.OPENSTACK_NOVA_DEFAULT_ENDPOINT.getParameter());
        }
        return clone;
    }

    @Override
    public void validate() throws ValidationException {
        //TODO validation exception to think of
    }
}
