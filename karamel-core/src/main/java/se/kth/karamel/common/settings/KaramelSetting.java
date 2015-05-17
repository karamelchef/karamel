package se.kth.karamel.common.settings;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 2015-05-14.
 */
public enum KaramelSetting {

    ATTR_DELIMITER("/"),
    COOOKBOOK_DELIMITER("::"),
    INSTALL_RECIPE("install"),
    CHEF_PRIVATE_IPS("private_ips"),
    CHEF_PUBLIC_IPS("public_ips"),
    CHEF_JSON_RUNLIST_TAG("run_list"),
    SLASH("/"),
    HTTP_PREFIX("http://"),
    HTTPS_PREFIX("https://");

    private String parameter;

    private static final Map<String,KaramelSetting> lookup
            = new HashMap<String,KaramelSetting>();

    static {
        for(KaramelSetting s : EnumSet.allOf(KaramelSetting.class))
            lookup.put(s.getParameter(), s);
    }

    private KaramelSetting(String parameter){
        this.parameter = parameter;
    }

    public String getParameter(){
        return parameter;
    }

    public static KaramelSetting get(String parameter) {
        return lookup.get(parameter);
    }
}
