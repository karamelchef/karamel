package se.kth.karamel.common.settings;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 2015-05-14.
 */
public enum OpenStackSetting {

    OPENSTACK_DEFAULT_FLAVOR("default flavor here!"),
    OPENSTACK_DEFAULT_REGION("default region here!"),
    OPENSTACK_DEFAULT_IMAGE("default image here!"),
    OPENSTACK_DEFAULT_USERNAME("default username here!");

    private String parameter;

    private static final Map<String, OpenStackSetting> lookup
            = new HashMap<String, OpenStackSetting>();

    static {
        for (OpenStackSetting s : EnumSet.allOf(OpenStackSetting.class))
            lookup.put(s.getParameter(), s);
    }

    private OpenStackSetting(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
