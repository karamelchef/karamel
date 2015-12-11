What is Karamel?
===================
Karamel is a  management tool for reproducibly deploying and provisioning distributed applications on bare-metal, cloud or  multi-cloud environments. Karamel provides explicit support for reproducible experiments for distributed systems. Users of Karamel experience the tool as an easy-to-use UI-driven approach to deploying distributed systems or running distributed experiments, where the deployed system or experiment can be easily configured via the UI. 

Karamel users can open a cluster definition file that describes a distributed system or experiment as:

* the application stacks used in the system, containing the set of services in each application stack,
* the provider(s) for each application stack in the cluster (the cloud provider or IP addresses of the bare-metal hosts),  
* the number of nodes that should be created and provisioned for each application stack,
* configuration parameters to customize each application stack.

Karamel is an orchestration engine that orchestrates:

* the creation of  virtual machines if a cloud provider is used;
* the global order for installing and starting services on each node;
* the injection of configuration parameters and passing of parameters between services.

  Karamel enables the deployment of arbitrarily large distributed systems on both virtualized platforms (AWS, Vagrant) and bare-metal hosts.

Karamel is built on the configuration framework, Chef_. The distributed system or experiment is defined in YAML as a set of node groups that each implement a number of Chef recipes, where the Chef cookbooks are deployed on github. Karamel orchestrates the execution of Chef recipes using a set of ordering rules defined in a YAML file (Karamelfile) in each cookbook. For each recipe, the Karamelfile can define a set of dependent (possibly external) recipes that should be executed before it. At the system level, the set of Karamelfiles defines a directed acyclic graph (DAG) of service dependencies. Karamel system definitions are very compact. We leverage Berkshelf to transparently download and install transitive cookbook dependencies, so large systems can be defined in a few lines of code. Finally, the Karamel runtime builds and manages the execution of the DAG of Chef recipes, by first launching the virtual machines or configuring the bare-metal boxes and then executing recipes with Chef Solo. The Karamel runtime executes the node setup steps using JClouds and Ssh. Karamel is agentless, and only requires ssh to be installed on the target host. Karamel transparently handles faults by retrying, as virtual machine creation or configuration is not always reliable or timely.

Existing Chef cookbooks can easily be ``karamelized``, that is, wrapped and extended with a Karamelfile containing orchestration rules. In contrast to Chef, which is used primarily to manage production clusters, Karamel is designed to support the creation of reproducible clusters for running experiments or benchmarks. Karamel provides additional Chef cookbook support for copying experiment results to persistent storage before tearing down clusters.


.. Infrastructure, software, parameters, data and experimenter are different elements involved in Karamelized experiments.

In Karamel, infrastructure and software are delivered as code while the cluster definitions can be configured by modifying the configuration parameters for the services containined in the cluster definition. Karamel uses Github as the artifact-server for Chef cookbooks, and all experiment artifacts are globally available - any person around the globe can replay/reproduce the construction of the distributed system.

Karamel leverages virtual-machines to provision infrastructures on different clouds. We have cloud-connectors for Amazon EC2, Google Compute Engine, OpenStack and on-premises (bare-metal).

.. Sofware definition in Karamel is made on top of Chef_ - Chef is dependency aware configuration and installation tool-set - while cluster-wide orchestration mechanism belongs to Karamel. 

.. In Karamel two level of development exist, to design and to run. Designing is a level of development to make a new system or experiments in a way that is runnable via Karamel, in that regard designer should have knowledge about Chef to some extent. Users are also somewhat developers but it is enough if you are familiar with our DSL for cluster definition.

   
.. _Chef: https://www.chef.io/
.. _hopsHadoop: https://github.com/hopshadoop


