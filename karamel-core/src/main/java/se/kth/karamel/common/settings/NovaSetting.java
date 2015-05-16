package se.kth.karamel.common.settings;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 2015-05-14.
 */
public enum NovaSetting {

    NOVA_DEFAULT_FLAVOR("default flavor here!"),
    NOVA_DEFAULT_REGION("default region here!"),
    NOVA_DEFAULT_IMAGE("default image here!"),
    NOVA_DEFAULT_USERNAME("default username here!"),
    NOVA_DEFAULT_ENDPOINT("default endpoint here!"),
    NOVA_ACCOUNT_ID_KEY("nova.account.id"),
    NOVA_ACCOUNT_ENDPOINT("nova.account.endpoint"),
    NOVA_ACCESSKEY_KEY("nova.access.key"),
    NOVA_KEYPAIR_NAME_KEY("nova.keypair.name");

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
