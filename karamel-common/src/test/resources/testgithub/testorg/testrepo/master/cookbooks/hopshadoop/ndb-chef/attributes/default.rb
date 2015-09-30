include_attribute "kagent"
include_attribute "btsync"

version="7"
majorVersion="4"
minorVersion="6"

versionStr                                          = "#{version}.#{majorVersion}.#{minorVersion}"
default[:ndb][:enabled]                             = "true"
default[:kagent][:enabled]                          = "false"
default[:ndb][:version]                             = versionStr
default[:ndb][:package_url]                        = "http://dev.mysql.com/get/Downloads/MySQL-Cluster-#{version}.#{majorVersion}/mysql-cluster-gpl-#{versionStr}-linux-glibc2.5-x86_64.tar.gz"
#default[:ndb][:package_url]                         = node[:download_url] + "/mysql-cluster-gpl-#{versionStr}-linux-glibc2.5-x86_64.tar.gz"
# checksum is not a security check - used to improve the speed of downloads by skipping if matched
# checksum calculated using: shasum -a 256 /var/www/hops/...tgz | cut -c-12
# checksum calculated using: sha256sum /var/www/hops/...tgz | cut -c-12
default[:ndb][:checksum]                            = ""

default[:ndb][:mgmd][:port]                         = 1186
default[:ndb][:ndbd][:port]                         = 10000
default[:ndb][:ip]                                  = "10.0.2.15"

default[:ndb][:loglevel]                            = "notice"
default[:ndb][:user]                                = "root"
default[:ndb][:group]                               = "root"
default[:ndb][:connectstring]                       = ""

default[:ndb][:DataMemory]                          = "80"
# Calculate IndexMemory size by default, can be overriden by user.
default[:ndb][:IndexMemory]                         = ""
default[:ndb][:NoOfReplicas]                        = "1"
default[:ndb][:FragmentLogFileSize]                 = "64M"
default[:ndb][:MaxNoOfAttributes]                   = "60000"
default[:ndb][:MaxNoOfTables]                       = "2024"
default[:ndb][:MaxNoOfOrderedIndexes]               = "256"
default[:ndb][:MaxNoOfUniqueHashIndexes]            = "128"
default[:ndb][:MaxDMLOperationsPerTransaction]      = "128"
default[:ndb][:TransactionBufferMemory]             = "1M"
default[:ndb][:MaxParallelScansPerFragment]         = "256"
default[:ndb][:MaxDiskWriteSpeed]                   = "20M"
default[:ndb][:MaxDiskWriteSpeedOtherNodeRestart]   = "50M"
default[:ndb][:MaxDiskWriteSpeedOwnRestart]         = "200M"
default[:ndb][:MinDiskWriteSpeed]                   = "5M"
default[:ndb][:DiskSyncSize]                        = "4M"
default[:ndb][:RedoBuffer]                          = "32M"
default[:ndb][:LongMessageBuffer]                   = "64M"
default[:ndb][:TransactionInactiveTimeout]          = "10000"
default[:ndb][:TransactionDeadlockDetectionTimeout] = "10000"
default[:ndb][:LockPagesInMainMemory]               = "1"
default[:ndb][:RealTimeScheduler]                   = "0"
default[:ndb][:SchedulerSpinTimer]                  = "0"
default[:ndb][:BuildIndexThreads]                   = "10"
default[:ndb][:CompressedLCP]                       = "0"
default[:ndb][:CompressedBackup]                    = "1"
default[:ndb][:BackupMaxWriteSize]                  = "1M"
default[:ndb][:BackupLogBufferSize]                 = "4M"
default[:ndb][:BackupDataBufferSize]                = "16M" 
default[:ndb][:BackupMemory]                        = "20M"
default[:ndb][:MaxAllocate]                         = "32M"
default[:ndb][:DefaultHashMapSize]                  = "3840"
default[:ndb][:ODirect]                             = "0"
default[:ndb][:SendBufferMemory]                    = "2M"
default[:ndb][:ReceiveBufferMemory]                 = "2M"

# Up to 8 execution threads supported
default[:ndb][:MaxNoOfExecutionThreads]             = ""
# Read up on this option first. Benefits from setting to "true" node[:ndb][:interrupts_isolated_to_single_cpu]
default[:ndb][:ThreadConfig]                        = ""


default[:ndb][:interrupts_isolated_to_single_cpu]   = "false"

default[:mgm][:scripts]            = %w{ enter-singleuser-mode.sh mgm-client.sh mgm-server-start.sh mgm-server-stop.sh mgm-server-restart.sh cluster-shutdown.sh cluster-init.sh cluster-start-with-recovery.sh exit-singleuser-mode.sh }
default[:ndb][:scripts]            = %w{ backup-start.sh backup-restore.sh ndbd-start.sh ndbd-init.sh ndbd-stop.sh ndbd-restart.sh }
default[:mysql][:scripts]          = %w{ get-mysql-socket.sh get-mysql-port.sh mysql-server-start.sh mysql-server-stop.sh mysql-server-restart.sh mysql-client.sh }
default[:memcached][:scripts]      = %w{ memcached-start.sh memcached-stop.sh memcached-restart.sh }


default[:ndb][:root_dir]           = "/var/lib/mysql-cluster"
default[:ndb][:log_dir]            = "/var/lib/mysql-cluster/log"
default[:ndb][:data_dir]           = "/var/lib/mysql-cluster/ndb_data"
default[:ndb][:version_dir]        = "/var/lib/mysql-cluster/ndb-#{versionStr}"
default[:ndb][:base_dir]           = "/var/lib/mysql-cluster/ndb"

default[:ndb][:scripts_dir]        = "/var/lib/mysql-cluster/ndb/scripts"
default[:ndb][:mgm_dir]            = "/var/lib/mysql-cluster/mgmd"

# MySQL Server Parameters
default[:ndb][:mysql_server_dir]   = "/var/lib/mysql-cluster/ndb/mysql"
default[:ndb][:num_ndb_slots_per_client]            = 1

# Max time that the mysqld and memcached will wait for the MySQL Cluster to be up and running.
# If the mysqld or memcached starts and the MySQL Cluster isn't running, it will not connect and will
# need to be restarted to connect to the cluster.
# Time in seconds
default[:ndb][:wait_startup]       = 300

# Base directory for MySQL binaries
default[:mysql][:root_dir]         = "/usr/local"
# Symbolic link to the current versioned mysql directory
default[:mysql][:base_dir]         = "#{node[:mysql][:root_dir]}/mysql"
# Concrete directory with mysql binaries for a specific mysql version
default[:mysql][:version_dir]      = "#{node[:mysql][:base_dir]}-" + versionStr

default[:mysql][:jdbc_url]         = ""

# MySQL Server Master-Slave replication binary log is enabled.
default[:mysql][:repl]             = "false"

# Username that the mysqld is run as.
default[:mysql][:run_as_user]      = "root"

# This is the username/password for any mysql server (mysqld) started.
# It is required by mysql clients to use the mysql server.
default[:mysql][:user]             = "kthfs"
default[:mysql][:password]         = "kthfs"

# Limit the number of mgm_servers to the range 49..51
default[:mgm][:id]                 = 49
# All mysqlds, memcacheds, and ndbclients (clusterj) are in the range 52..255
default[:mysql][:id]               = 52
# up to 65 memcacheds
default[:memcached][:id]           = 125
# up to 65 NameNodes
default[:nn][:id]                  = 190

# The address of the mysqld that will be used by hop
default[:ndb][:mysql_ip]           = "10.0.2.15"

# Size in MB of memcached cache
default[:memcached][:mem_size]     = 64
# See examples here for configuration: http://dev.mysql.com/doc/ndbapi/en/ndbmemcache-configuration.html
# options examples: ";dev=role"   or ";dev=role;S:c4,g1,t1" or ";S:c0,g1,t1" ";role=db-only"
default[:memcached][:options]      = ";role=ndb-caching;usec_rtt=250;max_tps=100000;m=#{default[:memcached][:mem_size]}"

#
# BitTorrent settings for copying NDB binaries
#
default[:btsync][:ndb][:lan_search_port] = 3837
default[:btsync][:ndb][:port]            = 44445
default[:btsync][:ndb][:seed_secret]     = ""
default[:btsync][:ndb][:leecher_secret]  = ""
default[:btsync][:ndb][:device_name]     = ""
# default[:btsync][:ndb][:seeder_ip]     = "#{default[:kagent][:dashboard_ip]}"
default[:btsync][:ndb][:leechers]        = ['10.0.2.15']
default[:ndb][:shared_folder]            = "#{node[:btsync][:shared_folder]}/ndb"

# IP addresses of the mgm-server, ndbds must be overridden by role/recipe caller.
default[:ndb][:public_ips]               = [''] 
default[:ndb][:private_ips]              = [''] 
default[:ndb][:mgmd][:public_ips]        = [''] 
default[:ndb][:mgmd][:private_ips]       = [''] 
default[:ndb][:ndbd][:public_ips]        = ['']
default[:ndb][:ndbd][:private_ips]       = ['']
default[:ndb][:mysqld][:public_ips]      = ['']
default[:ndb][:mysqld][:private_ips]     = ['']
default[:ndb][:memcached][:public_ips]   = ['']
default[:ndb][:memcached][:private_ips]  = ['']

default[:ndb][:ndbapi][:addrs]           = ['']

#default[:ndb][:dbt2_url]                 = "http://downloads.mysql.com/source/dbt2-0.37.50.3.tar.gz"
#default[:ndb][:sysbench_url]             = "http://downloads.mysql.com/source/sysbench-0.4.12.5.tar.gz"

default[:ndb][:mgmd][:public_key]        = ""