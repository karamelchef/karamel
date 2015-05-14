package se.kth.karamel.client.model;

import se.kth.karamel.common.exception.ValidationException;

/**
 * Created by alberto on 2015-05-14.
 */
public class OpenStack extends Provider{

    private String flavor;
    private String region;
    private String image;

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

    public static OpenStack makeDefault(){
        OpenStack openStack = new OpenStack();
        return openStack.applyDefaults();
    }

    @Override
    public OpenStack cloneMe() {
        OpenStack openStack = new OpenStack();
        openStack.setUsername(getUsername());
        openStack.setImage(image);
        openStack.setFlavor(flavor);
        openStack.setRegion(region);
        return openStack;
    }

    @Override
    public OpenStack applyParentScope(Provider parentScopeProvider) {
        OpenStack clone = cloneMe();
        if (parentScopeProvider instanceof OpenStack) {
            OpenStack parentOpenStack = (OpenStack) parentScopeProvider;
            if (clone.getUsername() == null) {
                clone.setUsername(parentOpenStack.getUsername());
            }
            if (clone.getImage() == null) {
                clone.setImage(parentOpenStack.getImage());
            }
            if (clone.getRegion() == null) {
                clone.setRegion(parentOpenStack.getRegion());
            }
        }
        return clone;
    }

    @Override
    public OpenStack applyDefaults() {
        OpenStack clone = cloneMe();
        //TODO add default settings for openstack
        if(clone.getUsername() == null){

        }
        if(clone.getImage() == null){

        }
        if(clone.getFlavor() == null){

        }
        if(clone.getRegion() == null){

        }
        return clone;
    }

    @Override
    public void validate() throws ValidationException {
        //TODO validation exception to think of
    }
}
