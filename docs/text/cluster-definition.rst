.. _cluster-definition:

Cluster Definition
==================

The cluster definition format is an expressive DSL based on YAML as you can see in the following sample. Since Karamel can run several clusters simultaneously, the name of each cluster must be unique.

Currently We support four cloud providers: Amazon EC2 (ec2), Google Compute Engine (gce), Openstack Nova (nova) and bare-metal(baremetal). You can define a provider globally within a cluster definition file or you can define a different provider for each group in the cluster definition file. In the group scope, you can overwrite some attributes of the network/machines in the global scope or you can choose an entirely different cloud provider, defining a multi-cloud deployment. Settings and properties for each provider is introduced in later section. For a single cloud deployment, one often uses group-scope provider details to override the type of instance used for machines in the group. For example, one group of nodes may require lots of memory and processing power, while other nodes require less. For AWS, you would achive this by overriding the ``instanceType`` attribute.

The Cookbooks section specifies GitHub references to the cookbooks used in the cluster definition. It is possible to refer to a specific version or branch for each GitHub repository.

We group machines based on the application stack (list of recipes) that should be installed on the machines in the group. The number of machines in each group and list of recipes must be defined under each group name. 

.. raw:: latex

    \newpage

.. code-block:: yaml

  name: spark
  ec2:
    type: m3.medium
    region: eu-west-1

  cookbooks: 
    hadoop: 
      github: "hopshadoop/apache-hadoop-chef"
    spark: 
      github: "hopshadoop/spark-chef"
      branch: "master"

  groups: 
    namenodes:
      size: 1
      recipes: 
        - hadoop::nn
        - hadoop::rm
        - hadoop::jhs
        - spark::master
    datanodes:
      size: 2
      recipes: 
        - hadoop::dn
        - hadoop::nm
        - spark::slave


AWS(Amazon EC2)
---------------
In cluster definitions, we use key word *ec2* for deploying the cluster on Amazon EC2 Cloud.  The following code snippet shows all supported attributes for AWS.

.. code-block:: yaml

  ec2:
    type: c4.large
    region: eu-west-1
    ami: ami-47a23a30
    price: 0.1
    vpc: vpc-f70ea392
    subnet: subnet-e7830290

`Type of the virtual machine <http://aws.amazon.com/ec2/instance-types/>`_, `region (data center) <http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html>`_ and `Amazon Machine Image <http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html>`_ are the basic properties.

We support `spot instances <http://aws.amazon.com/ec2/purchasing-options/spot-instances/>`_ that is a way to control your budget. Since Amazon prices are changing based on demand, price is a limit you can set if you are not willing to pay beyond that limit (price unit is USD).  

Virtual Private Cloud on AWS-EC2
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

We support `AWS VPC <http://aws.amazon.com/vpc/>`_ on EC2 for better performance. First you must define your VPC in EC2 with the following steps then include your vpc and subnet id in the cluster definition as it is shown above.  

1. Make a VPC and a subnet assigned to it under your ec2.
2. Check the "Auto-assign Public IP" item for your subnet. 
3. Make an internet gateway and attach it to the VPC.
4. Make a routing table for your VPC and add a row for your gateway into it, on this row open all ips '0.0.0.0/0'.
5. Add your vpc-id and subnet-id into the ec2 section of your yaml like the following example. Also make sure you are using the right image and type of instance for your vpc. 

Google Compute Engine
---------------------

To deploy the cluster on Google’s infrastructure, we use the keyword *gce* in the cluster definition YAML file. Following code snippet shows the current supported attributes:

.. code-block:: yaml

  gce:
   type: n1-standard-1
   zone: europe-west1-b
    image: ubuntu-1404-trusty-v20150316

`Machine type <https://cloud.google.com/compute/docs/machine-types>`_, `zone of the VMs <https://cloud.google.com/compute/docs/zones>`_, and the `VM image <https://cloud.google.com/compute/docs/images>`_ can be specified by the user.

Karamel uses Compute Engine’s OAuth 2.0 authentication method. Therefore, an OAuth 2.0 client ID needs to be created through the Google’s Developer Console. The description on how to generate a client ID is available `here <https://developers.google.com/console/help/new/?hl=en_US#generatingoauth2>`_. You need to select *Service account* as the application type. After generating a service account, click on *Generate new JSON key* button to download a generated JSON file that contains both private and public keys. You need to register the fullpath of the generated JSON file with Karamel API.

Bare-metal
----------
Bare-metal clusters are supported, but the machines must first be prepared with support for login using a ssh-key that is stored on the Karamel client. The target hosts must be contactable using ssh from the Karamel client, and the target hosts' ip-addresses must be specified in the cluster definition. If you have many ip-addresses in a range, it is possible to give range of addresses instead of specifying them one by one (the second example below). The public key stored on the Karamel client should be copied to the *.ssh/authorized_keys* file in the home folder of the sudo account on the target machines that will be used to install the software. The username goes into the cluster definition is the sudo account, and if there is a password required to get sudo access, it  must be entered in the Web UI or entered through Karamel's programmatic API.   

.. code-block:: yaml

  baremetal:
   username: ubuntu
   ips: 
    - 192.168.33.12
    - 192.168.33.13
    - 192.168.33.14
    - 192.168.44.15


IP-Range
~~~~~~~~
  
.. code-block:: yaml

  baremetal:
    username: ubuntu
    ips: 
    - 192.168.33.12-192.168.33.14
    - 192.168.44.15
