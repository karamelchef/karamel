package se.kth.karamel.common.settings;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 2015-05-14.
 */
public enum GitHubSetting {

    GITHUB_DOMAIN("github.com"),
    GITHUB_DEFAULT_BRANCH("master"),
    GITHUB_RAW_DOMAIN("raw.githubusercontent.com"),
    GITHUB_BASE_URL(KaramelSetting.HTTPS_PREFIX.getParameter() + GITHUB_DOMAIN.getParameter()),
    GITHUB_RAW_URL(KaramelSetting.HTTPS_PREFIX.getParameter() + GITHUB_RAW_DOMAIN.getParameter()),
    GITHUB_BASE_URL_PATTERN("http(s)?://github.com"),
    GITHUB_DEFAULT_REPO_URL1(GITHUB_BASE_URL.getParameter() + "/hopshadoop"),
    GITHUB_DEFAULT_REPO_URL2(GITHUB_BASE_URL.getParameter() + "/karamelize"),
    GITHUB_DEFAULT_REPO_URL3(GITHUB_BASE_URL.getParameter() + "/hopstart"),
    REPO_WITH_BRANCH_PATTERN("[^\\/]*/[^\\/]*/tree/[^\\/]*"),
    REPO_NO_BRANCH_PATTERN("[^\\/]*/[^\\/]*"),
    GITHUB_REPO_WITH_BRANCH_PATTERN("^" + GITHUB_BASE_URL_PATTERN.getParameter() + KaramelSetting.SLASH.getParameter() +
            REPO_WITH_BRANCH_PATTERN.getParameter() + "$"),
    GITHUB_REPO_NO_BRANCH_PATTERN("^" + GITHUB_BASE_URL_PATTERN.getParameter() + KaramelSetting.SLASH.getParameter() +
            REPO_NO_BRANCH_PATTERN.getParameter() + "$");

    private String parameter;

    private static final Map<String, GitHubSetting> lookup
            = new HashMap<String, GitHubSetting>();

    static {
        for (GitHubSetting s : EnumSet.allOf(GitHubSetting.class))
            lookup.put(s.getParameter(), s);
    }

    private GitHubSetting(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }

    public static GitHubSetting get(String parameter) {
        return lookup.get(parameter);
    }
}
