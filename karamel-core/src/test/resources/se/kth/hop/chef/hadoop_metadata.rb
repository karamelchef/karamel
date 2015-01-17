name             'hadoop'
maintainer       "Jim Dowling"
maintainer_email "jdowling@kth.se"
license          "GPL 2.0"
description      'Installs/Configures the Apache Hadoop distribution'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "1.0"

recipe            "hadoop::nn", "Installs a Hadoop Namenode"
recipe            "hadoop::dn", "Installs a Hadoop Namenode"
recipe            "hadoop::rm", "Installs a YARN ResourceManager"
recipe            "hadoop::nm", "Installs a YARN NodeManager"
recipe            "hadoop::jhs", "Installs a MapReduce History Server for YARN"
recipe            "hadoop::ps", "Installs a WebProxy Server for YARN"
recipe            "test"


depends 'hopagent'
depends 'java'
depends 'cmake'
depends 'apt'
depends 'yum'
depends 'build-essential'

%w{ ubuntu debian rhel centos }.each do |os|
  supports os
end

attribute "hadoop/version",   
:display_name => "Hadoop version",
:description => "Version of hadoop",
:type => 'string',
:default => "2.2.0"


attribute "hadoop/namenode/addrs"  , 
:display_name => "Namenode ip addresses (comma-separated)",
:description => "A comma-separated list of Namenode ip address",
:type => 'array',
:required => "required",
:default => ""

attribute "yarn/resourcemanager" ,  
:display_name => "Ip address",
:description => "Ip address for the resourcemanager",
:type => 'string',
:default => "",
:required => "recommended"

attribute "hadoop/user",
:display_name => "Username to run hadoop as",
:description => "Username to run hadoop as",
:type => 'string',
:default => "",
:required => "optional"

attribute "hadoop/format",
:display_name => "Format HDFS",
:description => "Format HDFS, Run 'hdfs namenode -format'",
:type => 'string',
:default => "true"

attribute 'hadoop/nn/public_ips',
:display_name => 'Public ips for NameNodes',
:description => 'Public ips of Namenodes',
:type => 'array',
:default => '[10.0.2.15]'

attribute 'hadoop/nn/private_ips',
:display_name => 'Public ips for NameNodes',
:description => 'Public ips of Namenodes',
:type => 'array',
:default => '[10.0.2.15]'

attribute 'hadoop/rm/public_ips',
:display_name => 'Public ips for ResourceManagers',
:description => 'Public ips of ResourceManagers',
:type => 'array',
:default => '[10.0.2.15]'

attribute 'hadoop/nn/private_ips',
:display_name => 'Public ips for ResourceManagers',
:description => 'Public ips of ResourceManagers',
:type => 'array',
:default => '[10.0.2.15]'

attribute 'hadoop/public_ips',
:display_name => 'Public ips for these nodes',
:description => 'Public ips of nodes in this group',
:type => 'array',
:default => '[10.0.2.15]'

attribute 'hadoop/private_ips',
:display_name => 'Private ips for these nodes',
:description => 'Private ips of nodes in this group',
:type => 'array',
:default => '[10.0.2.15]'