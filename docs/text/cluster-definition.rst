.. _cluster-definition:

Cluster Definition
==================

Cluster definition is an expressive DSL based on YAML as you can see in the following sample. Since Karamel can run several clusters simultaneously, name of the cluster must be unique in each Karamel-runtime.

Currently We support four cloud providers: Amazon EC2 (ec2), Google Compute Engine (gce), Openstack Nova (nova) and bare-metal(baremetal). You can define provider globally or per group. In the group scope, you can overwrite some attributes of the network/machines in the global scope or you can entirely choose another cloud provider, that's how we support multi-cloud deployment. Settings and properties for each provider is introduced in a separate sections following. 

 Cookbooks section introduces github references to the used cookbooks, it is also possible to refer to a specific version or branch for each github repository.

We group machines based on the software stack(list of recipes) that should be installed on them, number of machines in each group and list of recipes must be defined under each group name. 
 
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
In cluster definition we use key word *ec2* for deploying the cluster on Amazon EC2 Cloud.  Following code snippet shows all supported attributes for AWS.

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
In case of bare-metal muchies must be ready before hand and their ip-address are specified in the cluster definition. If you have many ip-addresses in a range, it is possible to give range of addresses instead of specifying them one by one (second following exmaple). Machines' credentials with super-user previlledges are required for establishing ssh connection. The username goes into the cluster definition while the sudo-password must be registered through our API.   

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
