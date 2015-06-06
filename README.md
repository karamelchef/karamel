# Karamel - the orchestrator for Chef

Website: http://www.karamel.io

Mailing list: <a href="https://groups.google.com/forum/#!forum/karamel-chef">Google Groups</a>

Karamel is an orchestration engine for Chef Solo that enables the deployment of large distributed systems on both virtualized platforms (AWS, Vagrant) and bare-metal hosts. 

A distributed system is defined in YAML as a set of node groups that each implement a number of Chef recipes, where the Chef cookbooks are deployed on github.  

Karamel introduces a Karamelfile to orchestrate the execution of Chef recipes. Karmelfiles are written in YAML and define dependencies between recipes, that is, the order in which Chef recipes should be run.

At the cluster level, the set of Karamelfiles defines a directed acyclic graph (DAG) of recipe dependencies. 

Karamel system definitions are very compact. We leverage Berkshelf to transparently download and install transitive cookbook dependencies, so large systems can be defined in a few lines of code. Finally, the Karamel runtime builds and manages the execution of the DAG of Chef recipes, by first launching the virtual machines or configuring the bare-metal boxes and then executing recipes with Chef Solo. 

Karamel provides a web-ui, built on AngularJS.

####Using Virtual Private Cloud on AWS-EC2
------

User must take the steps below to be able to use vpc: 

0. Make a VPC and a subnet assigned to it under your ec2.
1. Check the "Auto-assign Public IP" item for your subnet. 
2. Make an internet gateway and attach it to the VPC.
3. Make a routing table for your VPC and add a row for your gateway into it, on this row open all ips '0.0.0.0/0'.
4. Add your vpc-id and subnet-id into the ec2 section of your yaml like the following example. Also make sure you are using the right image and type of instance for your vpc. 

```yaml
ec2:
    type: c4.large
    region: eu-west-1
    image: ami-47a23a30
    vpc: vpc-f70ea392
    subnet: subnet-e7830290
```

### Build and run from Source

Ubuntu Requirements: 
     apt-get install lib32z1 lib32ncurses5 lib32bz2-1.0

Centos 7 Requirements:
Install zlib.i686, ncurses-libs.i686, and bzip2-libs.i686 on CentOS 7

Building from root directory:
     mvn install 

Running:
     cd karamel-ui/target/appassembler
     ./bin/karamel

