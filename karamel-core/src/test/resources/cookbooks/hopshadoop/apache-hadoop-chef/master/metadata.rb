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


depends 'kagent'
depends 'java'
depends 'cmake'
depends 'apt'
depends 'yum'
depends 'build-essential'
depends 'ark'

%w{ ubuntu debian rhel centos }.each do |os|
  supports os
end

attribute "hadoop/yarn/nm/memory_mbs",
:display_name => "Hadoop NodeManager Memory in MB",
:type => 'string',
:default => 3584

attribute "hadoop/yarn/vcores",
:display_name => "Hadoop NodeManager Number of Virtual Cores",
:type => 'string',
:default => 4

attribute "hadoop/version",
:display_name => "Hadoop version",
:description => "Version of hadoop",
:type => 'string',
:default => "2.6.0"

attribute "hadoop/yarn/user",
:display_name => "Username to run yarn as",
:description => "Username to run yarn as",
:type => 'string',
:default => "yarn"

attribute "hadoop/mr/user",
:display_name => "Username to run mapReduce as",
:description => "Username to run mapReduce as",
:type => 'string',
:default => "mapred"

attribute "hdfs/user",
:display_name => "Username to run hdfs as",
:description => "Username to run hdfs as",
:type => 'string',
:default => "hdfs"

attribute "hadoop/format",
:display_name => "Format HDFS",
:description => "Format HDFS, Run 'hdfs namenode -format",
:type => 'string',
:default => "true"

attribute "hadoop/mr/tmp_dir",
:display_name => "Hadoop Temp Dir",
:description => "The directory in which Hadoop stores temporary data, including container data",
:type => 'string',
:default => "/tmp/hadoop/mapreduce"

attribute "hadoop/data_dir",
:display_name => "HDFS Data Dir",
:description => "The directory in which Hadoop's DataNodes store their data",
:type => 'string',
:default => "/var/data/hadoop"

attribute "hadoop/yarn/nodemanager_hb_ms",
:description => "Heartbeat Interval for NodeManager->ResourceManager in ms",
:type => 'string',
:default => "1000"