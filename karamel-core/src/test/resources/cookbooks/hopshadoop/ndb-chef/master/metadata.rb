name             "ndb"
maintainer       "Jim Dowling"
maintainer_email "jdowling@kth.se"
license          "GPL 3.0"

description      "Installs/Configures NDB (MySQL Cluster)"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))

version          "1.0"

depends           "kagent"
depends           "btsync"

recipe            "ndb::install", "Installs MySQL Cluster binaries"

recipe            "ndb::mgmd", "Installs a MySQL Cluster management server (ndb_mgmd)"
recipe            "ndb::ndbd", "Installs a MySQL Cluster data node (ndbd)"
recipe            "ndb::mysqld", "Installs a MySQL Server connected to the MySQL Cluster (mysqld)"
recipe            "ndb::memcached", "Installs a memcached Server connected to the MySQL Cluster (memcached)"

recipe            "ndb::mgmd-purge", "Removes a MySQL Cluster management server (ndb_mgmd)"
recipe            "ndb::ndbd-purge", "Removes a MySQL Cluster data node (ndbd)"
recipe            "ndb::mysqld-purge", "Removes a MySQL Server connected to the MySQL Cluster (mysqld)"
recipe            "ndb::memcached-purge", "Removes a memcached Server connected to the MySQL Cluster (memcached)"

recipe            "ndb::btsync", "Installs MySQL Cluster binaries with BitTorrent (btsync)"

recipe            "ndb::purge", "Removes all data and all binaries related to a MySQL Cluster installation"



supports 'ubuntu', ">= 12.04"
supports 'rhel',   ">= 6.3"
supports 'centos',   ">= 6.3"
supports 'debian'

#
# Required Attributes
#


attribute "ndb/ports",
          :description => "Dummy ports",
          :type => 'array',
          :required => "required",
          :default => ['123', '134', '145']

attribute "ndb/DataMemory",
          :description => "Data memory for each MySQL Cluster Data Node",
          :type => 'string',
          :required => "required",
          :default => "80"

attribute "ndb/IndexMemory",
          :description => "Index memory for each MySQL Cluster Data Node",
          :type => 'string',
          :calculated => true

attribute "memcached/mem_size",
          :description => "Memcached data memory size",
          :type => 'string',
          :required => "required",
          :default => "80"

#
# Optional Attributes
#

#attribute "ndb/version",
#          :display_name => "Ndb version",
#          :description =>  "MySQL Cluster Version",
#          :required => "optional",
#          :type => 'string'


attribute "ndb/user",
          :description => "User that runs ndb database",
          :type => 'string', 
          :required => "optional",         
          :default => 'root'

attribute "ndb/group",
          :description => "Group that runs ndb database",
          :type => 'string',
          :required => "optional",          
          :default => 'root'

# attribute "mysql/user",
#           :description => "User that runs mysql server",
#           :required => "optional",
#           :type => 'string',
#           :default => 'hops'

# attribute "mysql/password",
#           :display_name => "Mysql password for hop user",
#           :description => "Password for hop mysql user",
#           :calculated => true,
#           :type => 'string'

# attribute "mysql/root/password",
#           :display_name => "MySQL server root password",
#           :description => "Password for the root mysql user",
#           :type => 'string',
#           :calculated => true

attribute "ndb/enabled",
          :description => "Set to true if using MySQL Cluster, false for standalone MySQL Server",
          :type => 'string',
          :default => "true"

attribute "ndb/root_dir",
          :description => "Install directory for MySQL Cluster data files",
          :type => 'string',
          :required => "optional",
          :default => "/var/lib/mysql-cluster"

attribute "mysql/base_dir",
          :description => "Install directory for MySQL Binaries",
          :type => 'string',
          :required => "optional",
          :default => "/usr/local"

attribute "ndb/mgm_server/port",
          :description => "Port used by Mgm servers in MySQL Cluster",
          :type => 'string',
          :required => "optional",
          :default => "1186"

attribute "ndb/NoOfReplicas",
          :description => "Num of replicas of the MySQL Cluster Data Nodes",
          :type => 'string',
          :required => "optional",
          :default => "1"

attribute "memcached/options",
          :description => "Memcached options",
          :type => 'string',
          :required => "optional",
          :default => ""

# attribute "btsync/ndb/seeder_secret",
# :display_name => "Ndb seeder's random secret key.",
# :description => "20 chars or more (normally 32 chars)",
# :type => 'string',
# :default => "AY27AAZKTKO3GONE6PBCZZRA6MKGRKBX2"

# attribute "btsync/ndb/leecher_secret",
# :display_name => "Ndb leecher's secret key.",
# :description => "Ndb's random secret (key) generated using the seeder's secret key. 20 chars or more (normally 32 chars)",
# :type => 'string',
# :default => "BTHKJKK4PIPIOJZ7GITF2SJ2IYDLSSJVY"

attribute "ndb/FragmentLogFileSize",
          :description => "FragmentLogFileSize",
          :type => 'string',
          :default =>  "64M"

attribute "ndb/MaxNoOfAttributes",
          :description => "MaxNoOfAttributes",
          :type => 'string',
          :default =>  "60000"

attribute "ndb/MaxNoOfTables",
          :description => "MaxNoOfTables",
          :type => 'string',
          :default =>  "2024"

attribute "ndb/MaxNoOfOrderedIndexes",
          :description => "MaxNoOfOrderedIndexes",
          :type => 'string',
          :default =>  "256"

attribute "ndb/MaxNoOfUniqueHashIndexes",
          :description => "MaxNoOfUniqueHashIndexes",
          :type => 'string',
          :default =>  "128"

attribute "ndb/MaxDMLOperationsPerTransaction",
          :description => "MaxDMLOperationsPerTransaction",
          :type => 'string',
          :default =>  "128"

attribute "ndb/TransactionBufferMemory",
          :description => "TransactionBufferMemory",
          :type => 'string',
          :default =>  "1M"

attribute "ndb/MaxParallelScansPerFragment",
          :description => "MaxParallelScansPerFragment",
          :type => 'string',
          :default =>  "256"

attribute "ndb/MaxDiskWriteSpeed",
          :description => "MaxDiskWriteSpeed",
          :type => 'string',
          :default =>  "20M"

attribute "ndb/MaxDiskWriteSpeedOtherNodeRestart",
          :description => "MaxDiskWriteSpeedOtherNodeRestart",
          :type => 'string',
          :default =>  "50M"

attribute "ndb/MaxDiskWriteSpeedOwnRestart",
          :description => "MaxDiskWriteSpeedOwnRestart",
          :type => 'string',
          :default =>  "200M"

attribute "ndb/MinDiskWriteSpeed",
          :description => "MinDiskWriteSpeed",
          :type => 'string',
          :default =>  "5M"

attribute "ndb/DiskSyncSize",
          :description => "DiskSyncSize",
          :type => 'string',
          :default =>  "4M"

attribute "ndb/RedoBuffer",
          :description => "RedoBuffer",
          :type => 'string',
          :default =>  "32M"

attribute "ndb/LongMessageBuffer",
          :description => "LongMessageBuffer",
          :type => 'string',
          :default =>  "64M"

attribute "ndb/TransactionInactiveTimeout",
          :description => "TransactionInactiveTimeout",
          :type => 'string',
          :default =>  "10000"

attribute "ndb/TransactionDeadlockDetectionTimeout",
          :description => "TransactionDeadlockDetectionTimeout",
          :type => 'string',
          :default =>  "10000"

attribute "ndb/LockPagesInMainMemory",
          :description => "LockPagesInMainMemory",
          :type => 'string',
          :default =>  "1"

attribute "ndb/RealTimeScheduler",
          :description => "RealTimeScheduler",
          :type => 'string',
          :default =>  "0"

attribute "ndb/SchedulerSpinTimer",
          :description => "SchedulerSpinTimer",
          :type => 'string',
          :default =>  "0"

attribute "ndb/BuildIndexThreads",
          :description => "BuildIndexThreads",
          :type => 'string',
          :default =>  "10"

attribute "ndb/CompressedLCP",
          :description => "CompressedLCP",
          :type => 'string',
          :default =>  "0"

attribute "ndb/CompressedBackup",
          :description => "CompressedBackup",
          :type => 'string',
          :default =>  "1"

attribute "ndb/BackupMaxWriteSize",
          :description => "BackupMaxWriteSize",
          :type => 'string',
          :default =>  "1M"

attribute "ndb/BackupLogBufferSize",
          :description => "BackupLogBufferSize",
          :type => 'string',
          :default =>  "4M"

attribute "ndb/BackupDataBufferSize",
          :description => "BackupDataBufferSize",
          :type => 'string',
          :default =>  "16M"

attribute "ndb/BackupMemory",
          :description => "BackupMemory",
          :type => 'string',
          :default =>  "20M"

attribute "ndb/MaxAllocate",
          :description => "MaxAllocate",
          :type => 'string',
          :default =>  "32M"

attribute "ndb/DefaultHashMapSize",
          :description => "DefaultHashMapSize",
          :type => 'string',
          :default =>  "3840"

attribute "ndb/ODirect",
          :description => "ODirect",
          :type => 'string',
          :default =>  "0"

attribute "ndb/SendBufferMemory",
          :description => "SendBufferMemory",
          :type => 'string',
          :default =>  "2M"

attribute "ndb/ReceiveBufferMemory",
          :description => "ReceiveBufferMemory",
          :type => 'string',
          :default =>  "2M"

attribute "kagent/enabled",
          :description =>  "Install kagent",
          :type => 'string',
          :required => "optional",
          :default => "false"
