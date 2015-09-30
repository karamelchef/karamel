default[:hadoop][:version]                 = "2.4.0"
default[:hdfs][:user]                      = "hdfs"
default[:hadoop][:group]                   = "hadoop"
default[:hadoop][:dir]                     = "/srv"
default[:hadoop][:home]                    = "#{node[:hadoop][:dir]}/hadoop-#{node[:hadoop][:version]}"
default[:hadoop][:logs_dir]                = "#{node[:hadoop][:home]}/logs"
default[:hadoop][:tmp_dir]                 = "#{node[:hadoop][:home]}/tmp"
default[:hadoop][:conf_dir]                = "#{node[:hadoop][:home]}/etc/hadoop"
default[:hadoop][:sbin_dir]                = "#{node[:hadoop][:home]}/sbin"
default[:hadoop][:data_dir]                = "/var/data/hadoop"
default[:hadoop][:dn][:data_dir]           = "#{node[:hadoop][:data_dir]}/hdfs/dn"
default[:hadoop][:nn][:name_dir]           = "#{node[:hadoop][:data_dir]}/hdfs/nn"

#default[:hadoop][:download_url]            = "#{node[:download_url]}/hadoop-#{node[:hadoop][:version]}.tar.gz"
#default[:hadoop][:download_url]            = "http://apache.mirror.digionline.de/hadoop/common/hadoop-#{node[:hadoop][:version]}/hadoop-#{node[:hadoop][:version]}.tar.gz"
default[:hadoop][:protobuf_url]            = "https://protobuf.googlecode.com/files/protobuf-2.5.0.tar.gz"
default[:hadoop][:hadoop_src_url]          = "http://apache.mirror.digionline.de/hadoop/common/hadoop-#{node[:hadoop][:version]}/hadoop-#{node[:hadoop][:version]}-src.tar.gz"
default[:hadoop][:nn][:http_port]          = 50070
default[:hadoop][:dn][:http_port]          = 50075
default[:hadoop][:nn][:port]               = 29211

default[:hadoop][:leader_check_interval_ms]= 1000
default[:hadoop][:missed_hb]               = 1
default[:hadoop][:num_replicas]            = 3
default[:hadoop][:db]                      = "hadoop"
default[:hadoop][:nn][:scripts]            = %w{ format-nn.sh start-nn.sh stop-nn.sh restart-nn.sh root-start-nn.sh hdfs.sh yarn.sh hadoop.sh } 
default[:hadoop][:dn][:scripts]            = %w{ start-dn.sh stop-dn.sh restart-dn.sh root-start-dn.sh hdfs.sh yarn.sh hadoop.sh } 
default[:hadoop][:max_retries]             = 0
default[:hadoop][:format]                  = "true"
default[:hadoop][:io_buffer_sz]            = 131072

default[:hadoop][:nn][:heap_size]          = 1000

default[:hadoop][:yarn][:scripts]          = %w{ start stop restart root-start }
default[:hadoop][:yarn][:user]             = "yarn"
default[:hadoop][:yarn][:nm][:memory_mbs]  = 3584
default[:hadoop][:yarn][:ps_port]          = 20888

default[:hadoop][:yarn][:vpmem_ratio]      = 4.1
default[:hadoop][:yarn][:vcores]           = 4
default[:hadoop][:yarn][:min_vcores]       = 1
default[:hadoop][:yarn][:max_vcores]       = 4
default[:hadoop][:yarn][:log_aggregation]  = "false"
default[:hadoop][:yarn][:log_retain_secs]  = 10800
default[:hadoop][:yarn][:log_retain_check] = 100

default[:hadoop][:yarn][:nodemanager_hb_ms]= "1000"

default[:hadoop][:am][:max_retries]        = 2

default[:hadoop][:yarn][:aux_services]     = "mapreduce_shuffle"
default[:hadoop][:mr][:shuffle_class]      = "org.apache.hadoop.mapred.ShuffleHandler"

default[:hadoop][:yarn][:app_classpath]    = "#{node[:hadoop][:home]}/etc/hadoop/, #{node[:hadoop][:home]}/share/hadoop/common/*, #{node[:hadoop][:home]}/share/hadoop/common/lib/*, #{node[:hadoop][:home]}/share/hadoop/hdfs/*, #{node[:hadoop][:home]}/share/hadoop/hdfs/lib/*, #{node[:hadoop][:home]}/share/hadoop/yarn/*, #{node[:hadoop][:home]}/share/hadoop/yarn/lib/*"#, #{node[:hadoop][:home]}, #{node[:hadoop][:home]}/lib/*, #{node[:hadoop][:home]}/share/hadoop/tools/lib/*, #{node[:hadoop][:home]}/share/hadoop/yarn/test/*, #{node[:hadoop][:home]}/share/hadoop/mapreduce/*, #{node[:hadoop][:home]}/share/hadoop/mapreduce/lib/*, #{node[:hadoop][:home]}/share/hadoop/mapreduce/test/*"

default[:hadoop][:rm][:addr]               = []
default[:hadoop][:rm][:http_port]          = 8088
default[:hadoop][:nm][:http_port]          = 8042
default[:hadoop][:jhs][:http_port]         = 19888


default[:hadoop][:mr][:staging_dir]        = "/user"
default[:hadoop][:mr][:tmp_dir]            = "/tmp/hadoop/mapreduce"

default[:hadoop][:jhs][:inter_dir]         = "/mr-history/done_intermediate"
default[:hadoop][:jhs][:done_dir]          = "/mr-history/done"

# YARN CONFIG VARIABLES
# http://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-common/yarn-default.xml
# If you need mapreduce, mapreduce.shuffle should be included here.
# You can have a comma-separated list of services
# http://hadoop.apache.org/docs/r2.1.0-beta/hadoop-mapreduce-client/hadoop-mapreduce-client-core/PluggableShuffleAndPluggableSort.html

default[:hadoop][:nn][:jmxport]            = "8077"
default[:hadoop][:rm][:jmxport]            = "8082"
default[:hadoop][:nm][:jmxport]            = "8083"

default[:hadoop][:jmx][:username]          = "monitorRole"
default[:hadoop][:jmx][:password]          = "hadoop"

default[:hadoop][:mr][:user]               = "mapred"

default[:hadoop][:nn][:public_ips]         = ['10.0.2.15']
default[:hadoop][:nn][:private_ips]        = ['10.0.2.15']
default[:hadoop][:dn][:public_ips]         = ['10.0.2.15']
default[:hadoop][:dn][:private_ips]        = ['10.0.2.15']
default[:hadoop][:rm][:public_ips]         = ['10.0.2.15']
default[:hadoop][:rm][:private_ips]        = ['10.0.2.15']
default[:hadoop][:nm][:public_ips]         = ['10.0.2.15']
default[:hadoop][:nm][:private_ips]        = ['10.0.2.15']
default[:hadoop][:jhs][:public_ips]        = ['10.0.2.15']
default[:hadoop][:jhs][:private_ips]       = ['10.0.2.15']
default[:hadoop][:ps][:public_ips]         = ['10.0.2.15']
default[:hadoop][:ps][:private_ips]        = ['10.0.2.15']

# comma-separated list of namenode addrs
default[:hadoop][:nn][:addrs]              = []

# build the native libraries. Is much slower, but removes warning when using services.
default[:hadoop][:native_libraries]        = "false"
default[:hadoop][:cgroups]                 = "false"

default[:kagent][:enabled]                 = "false"

default[:maven][:version]                  = "3.2.5"
default[:maven][:checksum]                 = ""


# https://github.com/caskdata/hadoop_wrapper_cookbook/blob/master/attributes/default.rb
default['hadoop']['yarn']['yarn.nodemanager.resource.memory-mb']                   = ""
default['hadoop']['yarn']['memory_percent']                                        = "75"