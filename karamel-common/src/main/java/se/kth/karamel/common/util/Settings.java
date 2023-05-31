/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author kamal
 */
public class Settings {

  private static final Logger logger = Logger.getLogger(Settings.class);

  //test
  public static boolean CB_CLASSPATH_MODE = false;
  // files
  public static boolean USE_CLONED_REPO_FILES = false;

  //read
  public static final String ATTR_DELIMITER = "/";
  public static final String COOKBOOK_DELIMITER = "::";
  public static final String INSTALL_RECIPE = "install";
  public static final String PURGE_RECIPE = "purge";
  public static final String HTTP_PREFIX = "http://";
  public static final String HTTPS_PREFIX = "https://";
  public static final String SLASH = "/";
  public static final String DOLLAR = "$";
  public static final String CARET = "^";
  public static final int DAY_IN_MS = 24 * 3600 * 1000;
  public static final int DAY_IN_MIN = 24 * 60;
  public static final int SEC_IN_MS = 1000;
  public static final int MIN_IN_MS = 60 * SEC_IN_MS;
  public static final String IP_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
      + "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
      + "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
      + "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

  //------------------------------Cluster Runtime Dynamics--------------------------------------------------------------
  public static final int INSTALLATION_DAG_THREADPOOL_SIZE = 100;
  public static final int CLUSTER_STATUS_CHECKING_INTERVAL = 1000;
  public static final int CLUSTER_FAILURE_DETECTION_INTERVAL = 5000;
  public static final int CLUSTER_STAT_REPORT_INTERVAL = Settings.MIN_IN_MS;
  public static final int MACHINE_TASKRUNNER_BUSYWAITING_INTERVALS = 100;
  public static final int MACHINES_TASKQUEUE_SIZE = 100;
  public static final int SSH_CONNECTION_TIMEOUT = DAY_IN_MS;
  public static final int SSH_SESSION_TIMEOUT = DAY_IN_MS;
  public static final int SSH_PING_INTERVAL = 10 * SEC_IN_MS;
  public static final int SSH_SESSION_RETRY_NUM = 10;
  public static final int SSH_CMD_RETRY_NUM = 2;
  public static final int SSH_CMD_RETRY_INTERVALS = 3 * SEC_IN_MS;
  public static final float SSH_CMD_RETRY_SCALE = 1.5f;
  public static final int SSH_CMD_MAX_TIOMEOUT = DAY_IN_MIN;
  public static final String CHEFDK_VERSION_KEY = "chefdk.version";
  public static final String CHEFDK_VERSION_DEFAULT = "3.7.23";

  //-----------------------------------------------JCLOUDS--------------------------------------------------------------
  public static final int JCLOUDS_PROPERTY_MAX_RETRIES = 100;
  public static final int JCLOUDS_PROPERTY_RETRY_DELAY_START = 1000; //ms
  public static final int EC2_MAX_FORK_VMS_PER_REQUEST = 50;

  //-------------------------------------------Shell script templates---------------------------------------------------
  public static final String SCRIPT_PATH_ROOT = "se/kth/karamel/backend/shellscripts/";
  public static final String SCRIPT_PATH_APTGET_ESSENTIALS = SCRIPT_PATH_ROOT + "aptget_essentials.sc";
  public static final String SCRIPT_FIND_OSTYPE = SCRIPT_PATH_ROOT + "find_ostype.sc";
  public static final String SCRIPT_PATH_SUDO_PASSWORD_CHECK = SCRIPT_PATH_ROOT + "sudo_password_check.sc";
  public static final String SCRIPT_PATH_CLONE_VENDOR_COOKBOOK = SCRIPT_PATH_ROOT + "clone_vendor_cookbook.sb";
  public static final String SCRIPT_PATH_PREPARE_STORAGE = SCRIPT_PATH_ROOT + "prepare_storages.sh";
  public static final String SCRIPT_CHECK_STICKYBIT_TMP = SCRIPT_PATH_ROOT + "check_stickybit_set_tmp.sh";
  public static final String SCRIPT_NAME_INSTALL_CHEFDK = "install_chefdk.sh";
  public static final String SCRIPT_PATH_INSTALL_CHEFDK = SCRIPT_PATH_ROOT
      + SCRIPT_NAME_INSTALL_CHEFDK;
  public static final String SCRIPT_PATH_MAKE_SOLO_RB = SCRIPT_PATH_ROOT + "make_solo_rb.sc";
  public static final String SCRIPT_PATH_RUN_RECIPE = SCRIPT_PATH_ROOT + "run_recipe.sc";
  public static final String SCRIPT_PATH_KILL_RUNNING_SESSION = SCRIPT_PATH_ROOT + "kill_current_session.sh";

  //----------------------------------------Providers General-----------------------------------------------------------
  public static final String UNIQUE_GROUP_NAME(String provider, String clusterName, String groupName) {
    return (provider + USER_NAME + "-" + clusterName + "-" + groupName).toLowerCase();
  }

  public static final List<String> UNIQUE_VM_NAMES(String provider, String clusterName, String groupName, int size) {
    List<String> names = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      names.add(UNIQUE_GROUP_NAME(provider, clusterName, groupName) + "-" + i);
    }
    return names;
  }

  public static final String PREPARE_STORAGES_KEY = "karamel.prepare.storages";
  public static final String PREPARE_STORAGES_DEFAULT = "true";
  public static final String SKIP_EXISTINGTASKS_KEY = "karamel.skip.existing.tasks";
  public static final String SKIP_EXISTINGTASKS_DEFAULT = "false";

  public static final String CHEF_FILE_CACHE_PATH = "chef.file_cache.path";
  public static final String CHEF_RUBYGEMS_URL = "chef.rubygems_url";
  public static final String CHEF_SUDO_BINARY = "chef.sudo_binary";
  public static final String DEFAULT_CHEF_SUDO_BINARY = "sudo";

  public static final String KARAMEL_AIRGAP = "karamel.airgap";
  public static final String DEFAULT_KARAMEL_AIRGAP = "false";
  private static final String KARAMEL_WORKING_DIRECTORY = "KARAMEL_WORKING_DIRECTORY";

  //--------------------------------------------Baremetal---------------------------------------------------------------
  public static final String PROVIDER_BAREMETAL_DEFAULT_USERNAME = "root";
  public static final int BAREMETAL_DEFAULT_SSH_PORT = 22;

  //------------------------------------Cookbooks on Github-------------------------------------------------------------
  public static final String CB_DEFAULTRB_REL_URL = "/attributes/default.rb";
  public static final String CB_METADATARB_REL_URL = "/metadata.rb";
  public static final String CB_KARAMELFILE_REL_URL = "/Karamelfile";
  public static final String CB_BERKSFILE_REL_URL = "/Berksfile";
  public static final String CB_CONFIGFILE = "config.props";
  public static final String CB_CONFIGFILE_REL_URL = "/templates/default/" + CB_CONFIGFILE;

  // ---------------------------------Cookbooks Scaffolding on Karamel Machine------------------------------------------
  public static final String CB_TEMPLATE_PATH_ROOT = "se" + File.separator + "kth" + File.separator + "karamel"
      + File.separator + "backend" + File.separator + "templates" + File.separator;
  public static final String CB_TEMPLATE_RECIPE_INSTALL = CB_TEMPLATE_PATH_ROOT + "recipe_install";
  public static final String CB_TEMPLATE_RECIPE_EXPERIMENT = CB_TEMPLATE_PATH_ROOT + "recipe_experiment";
  public static final String CB_TEMPLATE_CONFIG_PROPS = CB_TEMPLATE_PATH_ROOT + CB_CONFIGFILE;
  public static final String CB_TEMPLATE_KITCHEN_YML = CB_TEMPLATE_PATH_ROOT + "kitchen_yml";
  public static final String CB_TEMPLATE_METADATA = CB_TEMPLATE_PATH_ROOT + "metadata";
  public static final String CB_TEMPLATE_KARAMELFILE = CB_TEMPLATE_PATH_ROOT + "Karamelfile";
  public static final String CB_TEMPLATE_BERKSFILE = CB_TEMPLATE_PATH_ROOT + "Berksfile";
  public static final String CB_TEMPLATE_README = CB_TEMPLATE_PATH_ROOT + "README.md";
  public static final String CB_TEMPLATE_ATTRIBUTES_DEFAULT = CB_TEMPLATE_PATH_ROOT + "attributes_default";

  // Relative file locations of files in cookbook scaffolding
  public static final String COOKBOOK_DEFAULTRB_REL_PATH = File.separator + "attributes" + File.separator
      + "default.rb";
  public static final String COOKBOOK_METADATARB_REL_PATH = File.separator + "metadata.rb";
  public static final String COOKBOOK_BERKSFILE_REL_PATH = File.separator + "Berksfile";
  public static final String COOKBOOK_README_PATH = File.separator + "README.md";
  public static final String COOKBOOK_RECIPE_INSTALL_PATH = File.separator + "recipes" + File.separator
      + INSTALL_RECIPE + ".rb";

  public static final String METADATA_INCOMMENT_HOST_KEY = "%host%";

  //-----------------------------------------KANDY----------------------------------------------------------------------
  public static final String KANDY_REST_ROOT = "http://127.0.0.1:0/CloudServiceRecommender/api/cluster";
  public static final String KANDY_REST_STATS_STORE = KANDY_REST_ROOT + "/stats/store";
  public static final String KANDY_REST_CLUSTER_COST = KANDY_REST_ROOT + "/cost";

  public static final String KANDY_REST_STATS_UPDATE(String id) {
    return String.format("%s/stats/update/%s", KANDY_REST_ROOT, id);
  }

  //-----------------------------------------Github---------------------------------------------------------------------
  public static final String GITHUB_USER_KEY = "github.email";
  public static final String GITHUB_PASSWORD_KEY = "github.password";
  public static final String GITHUB_DOMAIN = "github.com";
  public static final String GITHUB_DEFAULT_BRANCH = "master";
  public static final String GITHUB_RAW_DOMAIN = "raw.githubusercontent.com";
  public static final String GITHUB_BASE_URL = HTTPS_PREFIX + GITHUB_DOMAIN;
  public static final String GITHUB_RAW_URL = HTTPS_PREFIX + GITHUB_RAW_DOMAIN;
  public static final String GITHUB_BASE_URL_PATTERN = "http(?:s)?:\\/\\/github.com";
  public static final Pattern REPO_WITH_SUBCOOKBOOK_PATTERN
      = Pattern.compile("([^\\/]*)\\/([^\\/]*)\\/tree\\/([^\\/]*)\\/(.*)");
  public static final Pattern REPO_WITH_BRANCH_PATTERN = Pattern.compile("([^\\/]*)\\/([^\\/]*)\\/tree\\/([^\\/]*)");
  public static final Pattern REPO_NO_BRANCH_PATTERN = Pattern.compile("([^\\/]*)\\/([^\\/]*)");
  public static final Pattern GITHUB_REPO_WITH_SUBCOOKBOOK_PATTERN = Pattern.compile(
      CARET + GITHUB_BASE_URL_PATTERN + "\\/" + REPO_WITH_SUBCOOKBOOK_PATTERN.pattern() + DOLLAR);
  public static final Pattern GITHUB_REPO_WITH_BRANCH_PATTERN = Pattern.compile(
      CARET + GITHUB_BASE_URL_PATTERN + SLASH + REPO_WITH_BRANCH_PATTERN.pattern() + DOLLAR);
  public static final Pattern GITHUB_REPO_NO_BRANCH_PATTERN = Pattern.compile(
      CARET + GITHUB_BASE_URL_PATTERN + SLASH + REPO_NO_BRANCH_PATTERN.pattern() + DOLLAR);

  //-----------------------------------------Machine General------------------------------------------------------------
  public static final String TMP_FOLDER_NAME = "tmp";
  public static final String PID_FILE_NAME = "pid";
  public static final String OSTYPE_FILE_NAME = "ostype";
  public static final String SYSTEM_TMP_FOLDER_PATH = "/" + TMP_FOLDER_NAME;
  public static final String SUCCEED_TASKLIST_FILENAME = "succeed_list";

  //--------------------------------Target Macines----------------------------------------------------------------------
  public static final String REMOTE_COOKBOOKS_DIR_NAME = "cookbooks";
  public static final String REMOTE_HOME_ROOT = "/home";
  public static final String REMOTE_CB_FS_PATH_DELIMITER = "__";
  public final static String REMOTE_CHEFJSON_PRIVATEIPS_TAG = "private_ips";
  public final static String REMOTE_CHEFJSON_PUBLICIPS_TAG = "public_ips";
  public final static String REMOTE_CHEFJSON_HOSTS_TAG = "hosts";
  public final static String REMOTE_CHEFJSON_PRIVATEIPS_DOMAIN_IDS_TAG =
      "private_ips_domainIds";
  
  public static final String REMOTE_CHEFJSON_RUNLIST_TAG = "run_list";
  public static final String REMOTE_WORKING_DIR_NAME = ".karamel";
  public static final String REMOTE_INSTALL_DIR_NAME = "install";

  public static String RECIPE_RESULT_REMOTE_PATH(String recipeName) {
    String recName;
    if (!recipeName.contains(COOKBOOK_DELIMITER)) {
      recName = recipeName + COOKBOOK_DELIMITER + "default";
    } else {
      recName = recipeName;
    }

    return Settings.SYSTEM_TMP_FOLDER_PATH + "/"
        + recName.replace(COOKBOOK_DELIMITER, REMOTE_CB_FS_PATH_DELIMITER) + RECIPE_RESULT_POSFIX;
  }

  public static String EXPERIMENT_RESULT_REMOTE_PATH(String recipeName) {
    String recName;
    if (!recipeName.contains(COOKBOOK_DELIMITER)) {
      recName = recipeName + COOKBOOK_DELIMITER + "default";
    } else {
      recName = recipeName;
    }

    return Settings.SYSTEM_TMP_FOLDER_PATH + "/" + recName.replace(COOKBOOK_DELIMITER, "_");
  }

  public static String REMOTE_USER_HOME_PATH(String sshUserName) {
    return USER_HOME != null ? USER_HOME : Paths.get(REMOTE_HOME_ROOT, sshUserName).toString();
  }

  public static String REMOTE_WORKING_DIR_PATH(String sshUserName) {
    String karamelWorkingDirectory = System.getenv(KARAMEL_WORKING_DIRECTORY);
    if (karamelWorkingDirectory != null) {
      return karamelWorkingDirectory;
    }
    return REMOTE_USER_HOME_PATH(sshUserName) + "/" + REMOTE_WORKING_DIR_NAME;
  }

  public static String REMOTE_INSTALL_DIR_PATH(String sshUserName) {
    return REMOTE_WORKING_DIR_PATH(sshUserName) + "/" + REMOTE_INSTALL_DIR_NAME;
  }

  public static String REMOTE_COOKBOOKS_PATH(String sshUserName) {
    return REMOTE_WORKING_DIR_PATH(sshUserName) + "/" + REMOTE_COOKBOOKS_DIR_NAME;
  }

  public static String REMOTE_COOKBOOK_VENDOR_PATH(String sshUserName, String repoName) {
    return REMOTE_COOKBOOKS_PATH(sshUserName) + "/" + repoName + "_vendor";
  }

  public static String REMOTE_SUCCEEDTASKS_PATH(String sshUserName) {
    return REMOTE_INSTALL_DIR_PATH(sshUserName) + "/" + SUCCEED_TASKLIST_FILENAME;
  }

  public static String REMOTE_OSTYPE_PATH(String sshUserName) {
    return REMOTE_INSTALL_DIR_PATH(sshUserName) + "/" + OSTYPE_FILE_NAME;
  }

  public static String REMOTE_PIDFILE_PATH(String sshUserName) {
    return REMOTE_INSTALL_DIR_PATH(sshUserName) + "/" + PID_FILE_NAME;
  }
  //------------------------------------------Karamel Machine-----------------------------------------------------------
  public static final String USER_HOME = System.getProperty("user.home");
  public static final String USER_NAME = System.getProperty("user.name");
  public static final String OS_NAME = System.getProperty("os.name");
  public static final String IP_ADDRESS = loadIpAddress();
  public static final boolean IS_UNIX_OS = OS_NAME.toLowerCase().contains("mac")
      || OS_NAME.toLowerCase().contains("linux");
  public static final String DEFAULT_PUBKEY_PATH = IS_UNIX_OS ? USER_HOME + "/.ssh/id_rsa.pub" : null;
  public static final String DEFAULT_PRIKEY_PATH = IS_UNIX_OS ? USER_HOME + "/.ssh/id_rsa" : null;
  public static final String SSH_PUBKEY_PATH_KEY = "ssh.publickey.path";
  public static final String SSH_PRIVKEY_PATH_KEY = "ssh.privatekey.path";
  public static final String TEST_CB_ROOT_FOLDER = "testgithub";
  public static final String COOKBOOKS_PATH = getKaramelRootPath() + File.separator + "cookbooks";
  public static final String YAML_FILE_NAME = "definition.yaml";
  public static final String KARAMEL_CONF_NAME = "conf";
  public static final String SSH_FOLDER_NAME = ".ssh";
  public static final String STATS_FOLDER_NAME = "stats";
  public static final String KARAMEL_SSH_PATH = getKaramelRootPath() + File.separator + SSH_FOLDER_NAME;
  public static final String KARAMEL_TMP_PATH = getKaramelRootPath() + File.separator + TMP_FOLDER_NAME;
  public static final String SSH_PUBKEY_FILENAME = "ida_rsa.pub";
  public static final String SSH_PRIVKEY_FILENAME = "ida_rsa";
  public static final String RECIPE_RESULT_POSFIX = "__out.json";

  public static String getKaramelRootPath() {
    String karamelWorkingDirectory = System.getenv(KARAMEL_WORKING_DIRECTORY);
    if (karamelWorkingDirectory != null) {
      return karamelWorkingDirectory;
    }
    return Paths.get(USER_HOME, ".karamel").toString();
  }

  public static String loadIpAddress() {
    String address = "UnknownHost";
    try {
      address = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException ex) {
    }
    return address;
  }

  public static String CLUSTER_LOG_FOLDER(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + "logs";
  }

  public static String MACHINE_LOG_FOLDER(String clusterName, String machineIp) {
    return CLUSTER_LOG_FOLDER(clusterName) + File.separator + machineIp;
  }

  public static String TASK_LOG_FILE_PATH(String clusterName, String machinIp, String taskName) {
    return MACHINE_LOG_FOLDER(clusterName, machinIp) + File.separator
        + taskName.toLowerCase().replaceAll("\\W", "_") + ".log";
  }

  public static String CLUSTER_ROOT_PATH(String clusterName) {
    return getKaramelRootPath() + File.separator + clusterName.toLowerCase();
  }

  public static String CLUSTER_SSH_PATH(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + SSH_FOLDER_NAME;
  }

  public static String CLUSTER_YAML_PATH(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + YAML_FILE_NAME;
  }

  public static String CLUSTER_STATS_FOLDER(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + STATS_FOLDER_NAME;
  }

  public static String CLUSTER_STATS_PATH(String clusterName, long statsName) {
    return CLUSTER_STATS_FOLDER(clusterName) + File.separator + statsName;
  }

  public static String RECIPE_CANONICAL_NAME(String recipeName) {
    if (!recipeName.contains(COOKBOOK_DELIMITER)) {
      return recipeName + COOKBOOK_DELIMITER + "default";
    } else {
      return recipeName;
    }
  }

  public static String CLUSTER_TEMP_FOLDER(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + "tmp";
  }

  public static String MACHINE_TEMP_FOLDER(String clusterName, String machineIp) {
    return CLUSTER_TEMP_FOLDER(clusterName) + File.separator + machineIp;
  }

  public static String MACHINE_SUCCEEDTASKS_PATH(String clusterName, String machineIp) {
    return MACHINE_TEMP_FOLDER(clusterName, machineIp) + File.separator + SUCCEED_TASKLIST_FILENAME;
  }

  public static String MACHINE_OSTYPE_PATH(String clusterName, String machineIp) {
    return MACHINE_TEMP_FOLDER(clusterName, machineIp) + File.separator + OSTYPE_FILE_NAME;
  }

  public static String RECIPE_RESULT_LOCAL_PATH(String recipeName, String clusterName, String machineIp) {
    String recName;
    if (!recipeName.contains(COOKBOOK_DELIMITER)) {
      recName = recipeName + COOKBOOK_DELIMITER + "default";
    } else {
      recName = recipeName;
    }
    return MACHINE_TEMP_FOLDER(clusterName, machineIp) + File.separator
        + recName.replace(COOKBOOK_DELIMITER, REMOTE_CB_FS_PATH_DELIMITER) + RECIPE_RESULT_POSFIX;
  }

  public static String EXPERIMENT_RESULT_LOCAL_PATH(String recipeName, String clusterName, String machineIp) {
    String recName;
    if (!recipeName.contains(COOKBOOK_DELIMITER)) {
      recName = recipeName + COOKBOOK_DELIMITER + "default";
    } else {
      recName = recipeName;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssZ");

    return getKaramelRootPath() + File.separator + "results" + File.separator + clusterName.toLowerCase()
        + File.separator + recName.replace(COOKBOOK_DELIMITER, REMOTE_CB_FS_PATH_DELIMITER) + File.separator
        + recName.replace(COOKBOOK_DELIMITER, REMOTE_CB_FS_PATH_DELIMITER) + "-"
        + sdf.format(new Date(System.currentTimeMillis())) + ".out";
  }

}
