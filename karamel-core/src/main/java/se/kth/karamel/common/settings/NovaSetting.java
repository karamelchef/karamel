package se.kth.karamel.common.settings;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 2015-05-14.
 */
public enum NovaSetting {

    OPENSTACK_NOVA_DEFAULT_FLAVOR("default flavor here!"),
    OPENSTACK_NOVA_DEFAULT_REGION("default region here!"),
    OPENSTACK_NOVA_DEFAULT_IMAGE("default image here!"),
    OPENSTACK_NOVA_DEFAULT_USERNAME("default username here!");

    private String parameter;

    private static final Map<String, NovaSetting> lookup
            = new HashMap<String, NovaSetting>();

    static {
        for (NovaSetting s : EnumSet.allOf(NovaSetting.class))
            lookup.put(s.getParameter(), s);
    }

    private NovaSetting(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
