/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import org.jclouds.aws.domain.Region;
import org.jclouds.ec2.domain.InstanceType;

/**
 *
 * @author kamal
 */
public class Settings {

  public static final String ATTR_DELIMITER = "/";
  public static final String COOOKBOOK_DELIMITER = "::";
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
  public static final String GITHUB_REPO_WITH_BRANCH_PATTERN = "^" + GITHUB_BASE_URL_PATTERN + "/" + REPO_WITH_BRANCH_PATTERN + "$";
  public static final String GITHUB_REPO_NO_BRANCH_PATTERN = "^" + GITHUB_BASE_URL_PATTERN + "/" + REPO_NO_BRANCH_PATTERN + "$";
  public static final String EC2_GEOUPNAME_PATTERN = "[a-z0-9][[a-z0-9]|[-]]*";

  public static final int INSTALLATION_DAG_THREADPOOL_SIZE = 100;
  public static final int SSH_CONNECT_RETRIES = 5;
  public static final int SSH_CONNECT_INTERVALS = 5 * 1000;
  public static final int SSH_PING_INTERVAL = 10 * 1000;
  public static final int MACHINE_TASKRUNNER_BUSYWAITING_INTERVALS = 100;
  public static final int TASK_BUSYWAITING_INTERVALS = 100;
  public static final int CLUSTER_FAILURE_DETECTION_INTERVAL = 5000;

  //Shell scripts
  public static final String SCRIPT_PATH_ROOT = "se/kth/karamel/backend/shellscripts/";
  public static final String SCRIPT_PATH_APTGET_ESSENTIALS = SCRIPT_PATH_ROOT + "aptget_essentials.sc";
  public static final String SCRIPT_PATH_CLONE_VENDOR_COOKBOOK = SCRIPT_PATH_ROOT + "clone_vendor_cookbook.sb";
  public static final String SCRIPT_NAME_INSTALL_RUBY_CHEF_BERKSHELF = "install_ruby_chef_berkshelf.sh";
  public static final String SCRIPT_PATH_INSTALL_RUBY_CHEF_BERKSHELF = SCRIPT_PATH_ROOT + SCRIPT_NAME_INSTALL_RUBY_CHEF_BERKSHELF;
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
  public static final boolean UNIX_OS = OS_NAME.toLowerCase().contains("mac") || OS_NAME.toLowerCase().contains("linux");
  public static final String DEFAULT_PUBKEY_PATH = UNIX_OS ? USER_HOME + "/.ssh/id_rsa.pub" : null;
  public static final String DEFAULT_PRIKEY_PATH = UNIX_OS ? USER_HOME + "/.ssh/id_rsa" : null;
  public static final String SSH_PUBKEY_PATH_KEY = "ssh.publickey.path";
  public static final String SSH_PRIKEY_PATH_KEY = "ssh.privatekey.path";
  public static final String EC2_ACCOUNT_ID_KEY = "ect2.account.id";
  public static final String EC2_ACCESSKEY_KEY = "ec2.access.key";
  public static final String EC2_KEYPAIR_NAME_KEY = "ec2.keypair.name";

  public static final String EC2_KEYPAIR_NAME(String clusterName) {
    return "karamel_" + USER_NAME + "_" + clusterName.toLowerCase() + "_" + OS_NAME + "_" + IP_Address;
  }
  public static final String KARAMEL_ROOT_PATH = USER_HOME + File.separator + ".karamel";
  public static final String YAML_FILE_NAME = "definition.yaml";
  public static final String KARAMEL_CONF_NAME = "conf";
  public static final String SSH_FOLDER_NAME = ".ssh";
  public static final String KARAMEL_SSH_PATH = KARAMEL_ROOT_PATH + File.separator + SSH_FOLDER_NAME;
  public static final String SSH_PUBKEY_FILENAME = "ida_rsa.pub";
  public static final String SSH_PRIKEY_FILENAME = "ida_rsa";

  public static String CLUSTER_ROOT_PATH(String clusterName) {
    return KARAMEL_ROOT_PATH + File.separator + clusterName.toLowerCase();
  }

  public static String CLUSTER_SSH_PATH(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + SSH_FOLDER_NAME;
  }

  public static String CLUSTER_YAML_PATH(String clusterName) {
    return CLUSTER_ROOT_PATH(clusterName) + File.separator + YAML_FILE_NAME;
  }
  public static final int EC2_RETRY_INTERVAL = 5 * 1000;
  public static final int EC2_RETRY_MAX = 100;
  public static final List<String> EC2_DEFAULT_PORTS = Arrays.asList(new String[]{"22"});
  public static final String VAGRANT_MACHINES_KEY = "vagrant.machines";

  public static final int MACHINES_TASKQUEUE_SIZE = 100;

  //Git cookbook metadata 
  public static final String COOKBOOK_DEFAULTRB_REL_PATH = "/attributes/default.rb";
  public static final String COOKBOOK_METADATARB_REL_PATH = "/metadata.rb";
  public static final String COOKBOOK_KARAMELFILE_REL_PATH = "/Karamelfile";
  public static final String COOKBOOK_BERKSFILE_REL_PATH = "/Berksfile";
  public static final String METADATA_INCOMMENT_HOST_KEY = "%host%";
  //settings on vm machines
  public static final String COOKBOOKS_ROOT_VENDOR_PATH = "/tmp/cookbooks";
  public static final String COOKBOOKS_VENDOR_SUBFOLDER = "berks-cookbooks";

  public static String loadIpAddress() {
    String address = "UnknownHost";
    try {
      address = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException ex) {
    }
    return address;
  }

}
