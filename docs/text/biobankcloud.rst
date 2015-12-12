.. _bbc:

Deploying BiobankCloud with Karamel
==========================================

BiobankCloud_ is a Platform-as-a-Service (PaaS) for biobanking with Big Data (Hadoop).
BiobankCloud_ brings together

* Hops Hadoop with
* SAASFEE, a Bioinformatics platform for YARN that provides both a workflow language (Cuneiform) and a 2nd-level scheduler (HiWAY)
* Charon, a cloud-of-clouds filesystem, for sharing data between BiobankCloud clusters.

We have written karamelized Chef cookbooks for installing all of the components of BiobankCloud, and we provide some sample cluster definitions for installing small, medium, and large BiobankCloud clusters. Users are, of course, expected to adapt these sample cluster definitions to their cloud provider or bare-metal environment as well as their needs.

The following is a brief description of the karmelized Chef cookbooks that we have developed to support the installation of BiobankCloud. The cookbooks are all publicly available at:  http://github.com/. 

* hopshadoop/apache-hadoop-chef
  
* hopshadoop/hops-hadoop-chef

* hopshadoop/elasticsearch-chef

* hopshadoop/ndb-chef

* hopshadoop/zeppelin-chef

* hopshadoop/hopsworks-chef

* hopshadoop/spark-chef

* hopshadoop/flink-chef

* biobankcloud/charon-chef

* biobankcloud/hiway-chef

  
The following is a cluster definition file that installs BiobankCloud on a single m3.xlarge instance on AWS/EC2:

.. code-block:: yaml

   name: BiobankCloudSingleNodeAws
   ec2:
       type: m3.xlarge
       region: eu-west-1
   cookbooks:                                                             
     hops: 
       github: "hopshadoop/hops-hadoop-chef"
       branch: "master"
     hadoop: 
       github: "hopshadoop/apache-hadoop-chef"
       branch: "master"
     hopsworks:                                                                     
       github: "hopshadoop/hopsworks-chef"
       branch: "master"  
     ndb:
       github: "hopshadoop/ndb-chef"
       branch: "master"
     spark:
       github: "hopshadoop/spark-chef"
       branch: "hops"
     zeppelin:
       github: "hopshadoop/zeppelin-chef"
       branch: "master"
     elastic:
       github: "hopshadoop/elasticsearch-chef"
       branch: "master"
     charon:
       github: "biobankcloud/charon-chef"
       branch: "master"
     hiway:
       github: "biobankcloud/hiway-chef"
       branch: "master"
   attrs:
     hdfs:
       user: glassfish
       conf_dir: /mnt/hadoop/etc/hadoop
     hadoop:
       dir: /mnt
       yarn: 
           user: glassfish
           nm:
             memory_mbs: 9600
           vcores: 4
       mr:
           user: glassfish
     spark:
       user: glassfish
     hiway:
       home: /mnt/hiway
       user: glassfish
       release: false
       hiway:
         am:
           memory_mb: '512'
           vcores: '1'
         worker:
           memory_mb: '3072'
           vcores: '1'
     hopsworks:
       user: glassfish
       twofactor_auth: "true"
     hops:
       use_hopsworks: "true"
     ndb:
       DataMemory: '50'
       IndexMemory: '15'
       dir: "/mnt"
       shared_folder: "/mnt"
     mysql:
       dir: "/mnt"
     charon:
       user: glassfish
       group: hadoop
       user_email: jdowling@kth.se
       use_only_aws: true
   groups: 
     master:
       size: 1 
       recipes:                                                                    
           - ndb::mysqld                                                       
           - ndb::mgmd
           - ndb::ndbd
           - hops::ndb
           - hops::rm
           - hops::nn
           - hops::dn
           - hops::nm
           - hopsworks
           - zeppelin
           - charon
           - elastic
           - spark::master
           - hiway::hiway_client
           - hiway::cuneiform_client
           - hiway::hiway_worker 
           - hiway::cuneiform_worker
           - hiway::variantcall_worker
   


The following is a cluster definition file that installs a very large, highly available, BiobankCloud cluster on 56 m3.xlarge instance on AWS/EC2:
	  
.. code-block:: yaml

   name: BiobankCloudMediumAws
   ec2:
       type: m3.xlarge
       region: eu-west-1
   cookbooks:                                                             
     hops: 
       github: "hopshadoop/hops-hadoop-chef"
       branch: "master"
     hadoop: 
       github: "hopshadoop/apache-hadoop-chef"
       branch: "master"
     hopsworks:                                                                     
       github: "hopshadoop/hopsworks-chef"
       branch: "master"  
     ndb:
       github: "hopshadoop/ndb-chef"
       branch: "master"
     spark:
       github: "hopshadoop/spark-chef"
       branch: "hops"
     zeppelin:
       github: "hopshadoop/zeppelin-chef"
       branch: "master"
     elastic:
       github: "hopshadoop/elasticsearch-chef"
       branch: "master"
     charon:
       github: "biobankcloud/charon-chef"
       branch: "master"
     hiway:
       github: "biobankcloud/hiway-chef"
       branch: "master"
   attrs:
     hdfs:
       user: glassfish
       conf_dir: /mnt/hadoop/etc/hadoop
     hadoop:
       dir: /mnt
       yarn: 
           user: glassfish
           nm:
             memory_mbs: 9600
           vcores: 8
       mr:
           user: glassfish
     spark:
       user: glassfish
     hiway:
       home: /mnt/hiway
       user: glassfish
       release: false
       hiway:
         am:
           memory_mb: '512'
           vcores: '1'
         worker:
           memory_mb: '3072'
           vcores: '1'
     hopsworks:
       user: glassfish
       twofactor_auth: "true"
     hops:
       use_hopsworks: "true"
     ndb:
       DataMemory: '8000'
       IndexMemory: '1000'
       dir: "/mnt"
       shared_folder: "/mnt"
     mysql:
       dir: "/mnt"
     charon:
       user: glassfish
       group: hadoop
       user_email: jdowling@kth.se
       use_only_aws: true
   groups: 
     master:
       size: 1 
       bbcui:
           - ndb::mgmd    
           - ndb::mysqld                                                       
           - hops::ndb      
           - hops::client
           - hopsworks
           - spark::yarn      
           - charon
           - zeppelin
           - hiway::hiway_client
           - hiway::cuneiform_client
     metadata:
       size: 2
       recipes:
           - hops::ndb      
           - hops::rm
           - hops::nn
           - ndb::mysqld
     elastic:
       size: 1
       recipes:
           - elastic
     database:
       size: 2
       recipes:
           - ndb::ndbd
     workers:
       size: 50
       recipes:
           - hops::ndb
           - hops::dn
           - hops::nm
           - hiway::hiway_worker 
           - hiway::cuneiform_worker
           - hiway::variantcall_worker
   
   
Alternative configurations are, of course, possible. You could run several Elasticsearch instances for high availability and more master instances if you have many active clients.

.. _BiobankCloud: https://www.biobankcloud.eu
