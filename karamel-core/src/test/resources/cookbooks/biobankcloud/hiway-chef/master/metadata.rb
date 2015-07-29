name             'hiway'
maintainer       "Marc Bux"
maintainer_email "bux@informatik.hu-berlin.de"
license          "Apache 2.0"
description      'Chef recipes for installing Hi-WAY, its dependencies, and several workflows.'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "1.0.0"

recipe           "kagent::install", "Installs the Karamel agent"
recipe           "hadoop::install", "Installs Hadoop 2 on the machines"
recipe           "kagent::default", "Configures the Karamel agent"
recipe           "hadoop::nn", "Installs a Hadoop Namenode"
recipe           "hadoop::rm", "Installs a YARN ResourceManager"
recipe           "hadoop::dn", "Installs a Hadoop Namenode"
recipe           "hadoop::nm", "Installs a YARN NodeManager"
recipe           "hadoop::jhs", "Installs a MapReduce History Server for YARN"
recipe           "hiway::install", "Installs and sets up Hi-WAY"
recipe           "hiway::hiway_client", "Configures Hadoop to support Hi-WAY on the Client"
recipe           "hiway::hiway_worker", "Configures Hadoop to support Hi-WAY on the Workers"
recipe           "hiway::galaxy_worker", "Installs, configures and updates Galaxy on the Workers"
recipe           "hiway::cuneiform_client", "Installs Cuneiform with all its dependencies on the Client"
recipe           "hiway::cuneiform_worker", "Installs Cuneiform with all its dependencies on the Workers"
recipe           "hiway::helloworld_client", "Prepares the Hello World Cuneiform workflow on the Client"
recipe           "hiway::helloworld_run_loc", "Runs the Hello World Cuneiform workflow locally on the Client via Cuneiform"
recipe           "hiway::wordcount_client", "Prepares the Word Count Cuneiform workflow on the Client"
recipe           "hiway::wordcount_run_loc", "Runs the Word Count Cuneiform workflow locally on the Client via Cuneiform"
recipe           "hiway::montage_synth_client", "Prepares the synthetic Montage DAX workflow on the Client"
recipe           "hiway::montage_synth_run_hw", "Runs the synthetic Montage DAX workflow on Hi-WAY from the Client"
recipe           "hiway::galaxy101_client", "Prepares the Galaxy 101 Galaxy workflow on the Client"
recipe           "hiway::galaxy101_worker", "Prepares the Galaxy 101 Galaxy workflow on the Workers"
recipe           "hiway::galaxy101_run_hw", "Runs the Galaxy 101 Galaxy workflow on Hi-WAY from the Client"
recipe           "hiway::variantcall_client", "Prepares the Variant Calling Cuneiform workflow on the Client"
recipe           "hiway::variantcall_worker", "Prepares the Variant Calling Cuneiform workflow on the Workers"
recipe           "hiway::variantcall_run_hw", "Runs the Variant Calling Cuneiform on Hi-WAY from the Client"
recipe           "hiway::RNAseq_client", "Prepares the TRAPLINE RNAseq Galaxy Workflow on the Client"
recipe           "hiway::RNAseq_worker", "Runs the TRAPLINE RNAseq Galaxy Workflow on Hi-WAY from the Client"
#recipe           "hiway::montage_m17_4_client", "Prepares the Montage DAX Workflow on the Client"
#recipe           "hiway::montage_m17_4_worker", "Prepares the Montage DAX Workflow on the Workers"

depends 'java'
depends 'git'
depends 'hadoop'
depends 'hops'
depends 'kagent'

%w{ ubuntu debian rhel centos }.each do |os|
  supports os
end

attribute "hiway/user",
:display_name => "Name of the Hi-WAY user",
:description => "Name of the Hi-WAY user",
:type => 'string',
:default => "hiway"

attribute "hiway/data",
:display_name => "Data directory",
:description => "Directory in which to store large data, e.g., input data of the workflow",
:type => 'string',
:default => "/home/hiway"

attribute "hiway/release",
:display_name => "Release or snaphsot",
:description => "Install Hi-WAY release as opposed to the latest snapshot version",
:type => 'string',
:default => "true"

attribute "hiway/hiway/am/memory_mb",
:display_name => "Hi-WAY Application Master Memory in MB",
:description => "Amount of memory in MB to be requested to run the application master.",
:type => 'string',
:default => 512

attribute "hiway/hiway/am/vcores",
:display_name => "Hi-WAY Application Master Number of Virtual Cores",
:description => "Hi-WAY Application Master Number of Virtual Cores",
:type => 'string',
:default => 1

attribute "hiway/hiway/worker/memory_mb",
:display_name => "Hi-WAY Worker Memory in MB",
:description => "Hi-WAY Worker Memory in MB",
:type => 'string',
:default => 1024

attribute "hiway/hiway/worker/vcores",
:display_name => "Hi-WAY Worker Number of Virtual Cores",
:description => "Hi-WAY Worker Number of Virtual Cores",
:type => 'string',
:default => 1

attribute "hiway/hiway/scheduler",
:display_name => "Hi-WAY Scheduler",
:description => "valid values: c3po, cloning, conservative, greedyQueue, heft, outlooking, placementAware, staticRoundRobin",
:type => 'string',
:default => "placementAware"

attribute "hiway/variantcall/reads/sample_id",
:display_name => "1000 Genomes Sample Id",
:description => "The Sample Id of sequence data from the 1000 Genomes project that is to be aligned",
:type => 'string',
:default => "HG02025"

attribute "hiway/variantcall/reads/run_ids",
:display_name => "1000 Genomes Run Ids",
:description => "The Run Ids of sequence data from the 1000 Genomes project that is to be aligned",
:type => 'array',
:default => ["SRR359188", "SRR359195"]

attribute "hiway/variantcall/reference/chromosomes",
:display_name => "HG38 chromosomes",
:description => "The chromosomes of the HG38 reference against which sequence data is to be aligned",
:type => 'array',
:default => ["chr22", "chrY"]