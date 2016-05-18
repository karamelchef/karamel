.. _getting-started:


*********************
Getting Started
*********************

How to run a cluster?
---------------------
.. include:: how-to-run-a-cluster.rst


Launching an Apache Hadoop Cluster with Karamel
---------------------------------------------------

A cluster definition file is shown below that defines a Apache Hadoop V2 cluster to be launched on AWS/EC2. If you click on ``Menu->Load Cluster Definition`` and open this file, you can then proceed to launch this Hadoop cluster by entering your AWS credentials and selecting or generating an Open Ssh keypair.

The cluster defintion includes a cookbook called 'hadoop', and recipes for HDFS' NameNode (**nn**) and DataNodes (**dn**), as well as YARN's ResourceManager (**rm**) and NodeManagers (**nm**) and finally a recipe for the MapReduce JobHistoryService (**jhs**). The **nn**, **rm**, and **jhs** recipes are included in a single group called 'metadata' group, and a single node will be created (size: 1) on which all three services will be installed and configured. On a second group (the datanodes group), **dn** and **nm** services will be installed and configured. They will will be installed on two nodes (size: 2). If you want more instances of a particular group, you simply increase the value of the size attribute, (e.g., set "size: 100" for the datanodes group if you want 100 data nodes and resource managers for Hadoop). Finally, we parameterize this cluster deployment with version 2.7.1 of Hadoop (attr -> hadoop -> version). The attrs section is used to supply parameters that are fed to chef recipes during installation.

.. code-block:: yaml
		   
  name: ApacheHadoopV2
  ec2:
      type: m3.medium
      region: eu-west-1
  cookbooks:
    hadoop:
      github: "hopshadoop/apache-hadoop-chef"
      version: "v0.1"
  attrs:
    hadoop:
      version: 2.7.1
  groups:
    metadata:
      size: 1
      recipes:
          - hadoop::nn
          - hadoop::rm
          - hadoop::jhs
    datanodes:
      size: 2
      recipes:
          - hadoop::dn
          - hadoop::nm
      
    
The cluster definition file also includes a cookbooks section. Github is our artifact server. We only support the use of cookbooks in our cluster definition file that are located on GitHub. Dependent cookbooks (through Berkshelf_) may also be used (from Opscode's repository, Chef supermarket or GitHub), but the cookbooks referenced in the YAML file must be hosted on GitHub. The reason for this is that the Karamel runtime uses Github APIs to query cookbooks for configuration parameters, available recipes, dependencies (Berksfile) and orchestration ruleModels (defined in a Karamelfile). The set of all Karamelfiles for all services is used to build a directed-acyclic graph (DAG) of the installation order for recipes. This allows for modular development and automatic composition of cookbooks into cluster, where each cookbook encapsulates its own orchestration ruleModels. In this way, deployment modules for complicated distributed systems can be developed and tested incrementally, where each service defines its own independent deployment model in Chef and Karamel, and independet deployment modules can be automatically composed into clusters in cluster definition files. This approach supports an incremental test and development model, helping improve the quality of deployment software.

.. _Berkshelf: https://berkshelf.com


Designing an experiment with Karamel/Chef
------------------------------------------------------
An experiment in Karamel is a cluster definition file that contains a recipe defining the experiment. As such, an experiment requires a Chef cookbook and recipe, and writing Chef cookbooks and recipes can be a daunting prospect for even experienced developers. Luckily, Karamel provides a UI that can take a bash script or a python program and generate a ``karamelized`` Chef cookbook with a Chef recipe for the experiment. The Chef cookbook is automatically uploaded to a GitHub repository that Karamel creates for you. You recipe may have dependencies on other recipes. For example, a MapReduce experiment defined on the above cluster should wait until all the other services have started before it runs. On examination of the Karamelfile for the hadoop cookbook, we can see that ``hadoop::jhs`` and ``hadoop::nm`` are the last services to start. Our MapReduce experiment can state in the Karamelfile that it should start after the ``hadoop::jhs`` and ``hadoop::nm`` services have started at all nodes in the cluster.

Experiments also have parameters and produce results. Karamel provides UI support for users to enter parameter values in the ``Configure`` menu item. An experiment can also download experiment results to your desktop (the Karamel client) by writing to the filename ``/tmp/<cookbook>__<recipe>.out``. For detailed information on how to design experiments, go to :ref:`experiment designer <experiment-designer>`


Designing an Experiment: MapReduce Wordcount 
---------------------------------------------------
.. include:: simple-experiment.rst

