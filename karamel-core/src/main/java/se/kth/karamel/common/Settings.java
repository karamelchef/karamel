/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jclouds.aws.domain.Region;
import org.jclouds.ec2.domain.InstanceType;

/**
 *
 * @author kamal
 */
public class Settings {

  //test
  public static boolean CB_CLASSPATH_MODE = false;
  public static final String TEST_CB_ROOT_FOLDER = "cookbooks";

  //read
  public static final String ATTR_DELIMITER = "/";
  public static final String COOOKBOOK_DELIMITER = "::";
  public static final String COOOKBOOK_FS_PATH_DELIMITER = "__";
  public static final String INSTALL_RECIPE = "install";
  public final static String CHEF_PRIVATE_IPS = "private_ips";
  public final static String CHEF_PUBLIC_IPS = "public_ips";
  public static final String CHEF_JSON_RUNLIST_TAG = "run_list";
  public static final String HTTP_PREFIX = "http://";
  public static final String HTTPS_PREFIX = "https://";
  public static final String GITHUB_DOMAIN = "github.com";
  public static final String GITHUB_DEFAULT_BRANCH = "master";
  public static final String SLASH = "/";
  public static final String GITHUB_RAW_DOMAIN = "raw.githubusercontent.com";
  public static final String GITHUB_BASE_URL = HTTPS_PREFIX + GITHUB_DOMAIN;
  public static final String GITHUB_RAW_URL = HTTPS_PREFIX + GITHUB_RAW_DOMAIN;
  public static final String GITHUB_BASE_URL_PATTERN = "http(s)?://github.com";
  public static final String GITHUB_DEFAULT_REPO_URL1 = GITHUB_BASE_URL + "/hopshadoop";
  public static final String GITHUB_DEFAULT_REPO_URL2 = GITHUB_BASE_URL + "/karamelize";
  public static final String GITHUB_DEFAULT_REPO_URL3 = GITHUB_BASE_URL + "/hopstart";
  public static final String REPO_WITH_BRANCH_PATTERN = "[^\\/]*/[^\\/]*/tree/[^\\/]*";
  public static final String REPO_NO_BRANCH_PATTERN = "[^\\/]*/[^\\/]*";
  public static final String GITHUB_REPO_WITH_BRANCH_PATTERN = "^"
      + GITHUB_BASE_URL_PATTERN + "/" + REPO_WITH_BRANCH_PATTERN + "$";
  public static final String GITHUB_REPO_NO_BRANCH_PATTERN = "^"
      + GITHUB_BASE_URL_PATTERN + "/" + REPO_NO_BRANCH_PATTERN + "$";
  public static final String EC2_GEOUPNAME_PATTERN = "[a-z0-9][[a-z0-9]|[-]]*";

  public static final int INSTALLATION_DAG_THREADPOOL_SIZE = 100;
  public static final int SSH_CONNECT_RETRIES = 5;
  public static final int SSH_CONNECT_INTERVALS = 5 * 1000;
  public static final int SSH_PING_INTERVAL = 10 * 1000;
  public static final int MACHINE_TASKRUNNER_BUSYWAITING_INTERVALS = 100;
  public static final int CLUSTER_STATUS_CHECKING_INTERVAL = 1000;
  public static final int CLUSTER_FAILURE_DETECTION_INTERVAL = 5000;
  public static final int SSH_CONNECTION_TIMEOUT = 24 * 3600 * 1000;
  public static final int SSH_SESSION_TIMEOUT = 24 * 3600 * 1000;

  //Jcloud settings
  public static final int JCLOUDS_PROPERTY_MAX_RETRIES = 100;
  public static final int JCLOUDS_PROPERTY_RETRY_DELAY_START = 1000; //ms
  public static final int EC2_MAX_FORK_VMS_PER_REQUEST = 50;

  //Shell scripts
  public static final String SCRIPT_PATH_ROOT = "se/kth/karamel/backend/shellscripts/";
  public static final String SCRIPT_PATH_APTGET_ESSENTIALS = SCRIPT_PATH_ROOT + "aptget_essentials.sc";
  public static final String SCRIPT_PATH_CLONE_VENDOR_COOKBOOK = SCRIPT_PATH_ROOT + "clone_vendor_cookbook.sb";
  public static final String SCRIPT_NAME_INSTALL_RUBY_CHEF_BERKSHELF = "install_ruby_chef_berkshelf.sh";
  public static final String SCRIPT_PATH_INSTALL_RUBY_CHEF_BERKSHELF = SCRIPT_PATH_ROOT
      + SCRIPT_NAME_INSTALL_RUBY_CHEF_BERKSHELF;
  public static final String SCRIPT_PATH_MAKE_SOLO_RB = SCRIPT_PATH_ROOT + "make_solo_rb.sc";
  public static final String SCRIPT_PATH_RUN_RECIPE = SCRIPT_PATH_ROOT + "run_recipe.sc";

  //Providers 
  public static final String PROVIDER_EC2_DEFAULT_TYPE = InstanceType.M1_MEDIUM;
  public static final String PROVIDER_EC2_DEFAULT_REGION = Region.EU_WEST_1;
  //  public static final String PROVIDER_EC2_DEFAULT_IMAGE = "ami-0307ce74"; //12.04  "ami-896c96fe"; // 14.04
  public static final String PROVIDER_EC2_DEFAULT_IMAGE = "ami-0307ce74"; //12.04  "ami-896c96fe"; // 14.04 
  public static final String PROVIDER_EC2_DEFAULT_USERNAME = "ubuntu";
  public static final String PROVIDER_BAREMETAL_DEFAULT_USERNAME = "root";

  public static final String USER_HOME = System.getProperty("user.home");
  public static final String USER_NAME = System.getProperty("user.name");
  public static final String OS_NAME = System.getProperty("os.name");
  public static final String IP_Address = loadIpAddress();
  public static final boolean UNIX_OS = OS_NAME.toLowerCase().contains("mac")
      || OS_NAME.toLowerCase().contains("linux");
  public static final String DEFAULT_PUBKEY_PATH = UNIX_OS ? USER_HOME + "/.ssh/id_rsa.pub" : null;
  public static final String DEFAULT_PRIKEY_PATH = UNIX_OS ? USER_HOME + "/.ssh/id_rsa" : null;
  public static final String SSH_PUBKEY_PATH_KEY = "ssh.publickey.path";
  public static final String SSH_PRIVKEY_PATH_KEY = "ssh.privatekey.path";
  public static final String SSH_PRIVKEY_PASSPHRASE = "ssh.privatekey.passphrase";
  public static final String AWS_ACCESS_KEY = "aws.access.key";
  public static final String AWS_ACCESS_KEY_ENV_VAR = "AWS_ACCESS_KEY_ID";
  public static final String AWS_SECRET_KEY = "aws.secret.key";
  public static final String AWS_SECRET_KEY_ENV_VAR = "AWS_SECRET_ACCESS_KEY";
  public static final String AWS_KEYPAIR_NAME_KEY = "aws.keypair.name";

  public static final String EC2_KEYPAIR_NAME(String clusterName, String region) {
    return (USER_NAME + "-" + clusterName + "-" + region + "-" + OS_NAME + "-" + IP_Address).toLowerCase();
  }

  public static final String EC2_UNIQUE_GROUP_NAME(String clusterName, String groupName) {
    return (USER_NAME + "-" + clusterName + "-" + groupName).toLowerCase();
  }

  public static final List<String> EC2_UNIQUE_VM_NAMES(String clusterName, String groupName, int size) {
    List<String> names = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      names.add(EC2_UNIQUE_GROUP_NAME(clusterName, groupName) + "-" + i);
    }
    return names;
  }

  public static final String KARAMEL_ROOT_PATH = USER_HOME + File.separator + ".karamel";
  public static final String YAML_FILE_NAME = "definition.yaml";
  public static final String KARAMEL_CONF_NAME = "conf";
  public static final String SSH_FOLDER_NAME = ".ssh";
  public static final String TMP_FOLDER_NAME = "tmp";
  public static final String SYSTEM_TMP_FOLDER_PATH = "/" + TMP_FOLDER_NAME;
  public static final String KARAMEL_SSH_PATH = KARAMEL_ROOT_PATH + File.separator + SSH_FOLDER_NAME;
  public static final String KARAMEL_TMP_PATH = KARAMEL_ROOT_PATH + File.separator + TMP_FOLDER_NAME;
  public static final String SSH_PUBKEY_FILENAME = "ida_rsa.pub";
  public static final String SSH_PRIVKEY_FILENAME = "ida_rsa";
  public static final String RECIPE_RESULT_POSFIX = "__out.json";

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
    return KARAMEL_ROOT_PATH + File.separator + clusterName.toLowerCase();
  }

  public static String CLUSTER_SSH_PATH(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + SSH_FOLDER_NAME;
  }

  public static String CLUSTER_YAML_PATH(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + YAML_FILE_NAME;
  }

  public static String RECIPE_CANONICAL_NAME(String recipeName) {
    if (!recipeName.contains(COOOKBOOK_DELIMITER)) {
      return recipeName + COOOKBOOK_DELIMITER + "default";
    } else {
      return recipeName;
    }
  }

  public static String RECIPE_RESULT_REMOTE_PATH(String recipeName) {
    String recName;
    if (!recipeName.contains(COOOKBOOK_DELIMITER)) {
      recName = recipeName + COOOKBOOK_DELIMITER + "default";
    } else {
      recName = recipeName;
    }

    return Settings.SYSTEM_TMP_FOLDER_PATH + File.separator
        + recName.replace(COOOKBOOK_DELIMITER, COOOKBOOK_FS_PATH_DELIMITER) + RECIPE_RESULT_POSFIX;
  }

  public static String CLUSTER_TEMP_FOLDER(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + "tmp";
  }

  public static String MACHINE_TEMP_FOLDER(String clusterName, String machineIp) {
    return CLUSTER_TEMP_FOLDER(clusterName) + File.separator + machineIp;
  }

  public static String RECIPE_RESULT_LOCAL_PATH(String recipeName, String clusterName, String machineIp) {
    String recName;
    if (!recipeName.contains(COOOKBOOK_DELIMITER)) {
      recName = recipeName + COOOKBOOK_DELIMITER + "default";
    } else {
      recName = recipeName;
    }
    return MACHINE_TEMP_FOLDER(clusterName, machineIp) + File.separator
        + recName.replace(COOOKBOOK_DELIMITER, COOOKBOOK_FS_PATH_DELIMITER) + RECIPE_RESULT_POSFIX;
  }

  public static final int EC2_RETRY_INTERVAL = 5 * 1000;
  public static final int EC2_RETRY_MAX = 100;
  public static final List<String> EC2_DEFAULT_PORTS = Arrays.asList(new String[]{"22"});
  public static final String VAGRANT_MACHINES_KEY = "vagrant.machines";

  public static final int MACHINES_TASKQUEUE_SIZE = 100;

  public static final int SSH_CMD_RETRY_NUM = 2;
  public static final int SSH_CMD_RETRY_INTERVALS = 3000; //ms
  public static final float SSH_CMD_RETRY_SCALE = 1.5f;
  public static final int SSH_CMD_LONGEST = 24 * 60; // minutes

  //Git cookbook metadata 
  public static final String COOKBOOK_DEFAULTRB_REL_URL = "/attributes/default.rb";
  public static final String COOKBOOK_METADATARB_REL_URL = "/metadata.rb";
  public static final String COOKBOOK_KARAMELFILE_REL_URL = "/Karamelfile";
  public static final String COOKBOOK_BERKSFILE_REL_URL = "/Berksfile";

  // Template files for generating scaffolding for a cookbook. Taken from src/resources folder.
  public static final String CB_TEMPLATE_PATH_ROOT = "se/kth/karamel/backend/templates/";
  public static final String CB_TEMPLATE_RECIPE_INSTALL = CB_TEMPLATE_PATH_ROOT + "recipe_install";
  public static final String CB_TEMPLATE_RECIPE_DEFAULT = CB_TEMPLATE_PATH_ROOT + "recipe_default";
  public static final String CB_TEMPLATE_RECIPE_MASTER = CB_TEMPLATE_PATH_ROOT + "recipe_master";
  public static final String CB_TEMPLATE_RECIPE_SLAVE = CB_TEMPLATE_PATH_ROOT + "recipe_slave";
  public static final String CB_TEMPLATE_CONFIG_PROPS = CB_TEMPLATE_PATH_ROOT + "config.props";
  public static final String CB_TEMPLATE_KITCHEN_YML = CB_TEMPLATE_PATH_ROOT + "kitchen_yml";
  public static final String CB_TEMPLATE_MASTER_SH = CB_TEMPLATE_PATH_ROOT + "master_sh";
  public static final String CB_TEMPLATE_SLAVE_SH = CB_TEMPLATE_PATH_ROOT + "slave_sh";
  public static final String CB_TEMPLATE_METADATA = CB_TEMPLATE_PATH_ROOT + "metadata";
  public static final String CB_TEMPLATE_KARAMELFILE = CB_TEMPLATE_PATH_ROOT + "Karamelfile";
  public static final String CB_TEMPLATE_BERKSFILE = CB_TEMPLATE_PATH_ROOT + "Berksfile";
  public static final String CB_TEMPLATE_ATTRIBUTES_DEFAULT = CB_TEMPLATE_PATH_ROOT + "attributes_default";

  // Relative file locations of files in cookbook scaffolding
  public static final String COOKBOOK_DEFAULTRB_REL_PATH = File.separator + "attributes" + File.separator
      + "default.rb";
  public static final String COOKBOOK_METADATARB_REL_PATH = File.separator + "metadata.rb";
  public static final String COOKBOOK_KARAMELFILE_REL_PATH = File.separator + "Karamelfile";
  public static final String COOKBOOK_BERKSFILE_REL_PATH = File.separator + "Berksfile";
  public static final String COOKBOOK_RECIPE_INSTALL_PATH = File.separator + "recipes" + File.separator + "install.rb";
  public static final String COOKBOOK_RECIPE_DEFAULT_PATH = File.separator + "recipes" + File.separator + "default.rb";
  public static final String COOKBOOK_RECIPE_MASTER_PATH = File.separator + "recipes" + File.separator + "master.rb";
  public static final String COOKBOOK_RECIPE_SLAVE_PATH = File.separator + "recipes" + File.separator + "slave.rb";
  public static final String COOKBOOK_CONFIG_FILE_PATH = File.separator + "templates" + File.separator + "default"
      + File.separator + "config.props.erb";
  public static final String COOKBOOK_MASTER_SH_PATH = File.separator + "templates" + File.separator + "default"
      + File.separator + "master.sh.erb";
  public static final String COOKBOOK_SLAVE_SH_PATH = File.separator + "templates" + File.separator + "default"
      + File.separator + "slave.sh.erb";
  public static final String COOKBOOK_KITCHEN_YML_PATH = File.separator + ".kitchen.yml";

  public static final String METADATA_INCOMMENT_HOST_KEY = "%host%";
  //settings on vm machines
  public static final String COOKBOOKS_ROOT_VENDOR_PATH = "/tmp/cookbooks";
  public static final String COOKBOOKS_VENDOR_SUBFOLDER = "berks-cookbooks";

  public static final String IP_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
      + "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
      + "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
      + "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

  public static String loadIpAddress() {
    String address = "UnknownHost";
    try {
      address = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException ex) {
    }
    return address;
  }

  public static final int BAREMETAL_DEFAULT_SSH_PORT = 22;

}
