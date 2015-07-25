# Karamel - Reproducing Distributed Systems and Experiments on Cloud

Website: http://www.karamel.io

Mailing list: <a href="https://groups.google.com/forum/#!forum/karamel-chef">Google Groups</a>

Karamel is a set of management tools for provisioning, orchestration, monitoring and debugging of large distributed systems and experiments. We support public clouds (Amazon EC2 and Google Compute Engine), general purpose cloud manager (Openstack Nova) and in-house premises (bare-metal).

Karamel provisions network and virtual machines via cloud-providers' API, _physical_ _layer_, while installation and running experiments happens through direct ssh connection with virtual machines, _sofware_ _layer_. Therefore for adding a support for new cloud-provider it is enough to be handled in the physical layer because software layer is general purpose. 

In Karamel software packages are defined in Chef cookbooks, each cookbook is deployed separately in Github. Each cookbook contains set of recipes and recipe is a unit of software for installing a service or running an experiment. Some of our predefined cookbooks can be found <a href="https://github.com/hopshadoop">here</a>.

Karamel introduces Karamelfile to orchestrate the execution of Chef recipes. Karmelfiles are written in YAML and define dependencies between recipes, that is, the order in which Chef recipes should be run. At the cluster level, the set of Karamelfiles defines a directed acyclic graph (DAG) of recipe dependencies. 

We leverage Berkshelf to transparently download and install transitive cookbook dependencies, so large systems can be defined in a few lines of code. Finally, the Karamel runtime builds and manages the execution of the DAG of Chef recipes by executing them with Chef Solo. 

### Cluster Definition
---
Cluster definition is an expressive DSL based on YAML as you can see in the following sample. Since Karamel can run several clusters simultaneously, name of the cluster must be unique in each Karamel-runtime.

We support four cloud providers: Amazon EC2 (ec2), Google Compute Engine (gce), Openstack Nova (nova) and bare-metal(baremetal). You can define provider globally or per group. In the group scope, you can overwrite some attributes of the network/machines in the global scope or you can entirely choose another cloud provider, that's how we support multi-cloud deployment. Settings and properties for each provider is introduced in a separate sections following. 

 Cookbooks section introduces github references to the used cookbooks, it is also possible to refer to a specific version or branch for each github repository.

We group machines based on the software stack(list of recipes) that should be installed on them, number of machines in each group and list of recipes must be defined under each group name. 
 
```yaml
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
```

####AWS(Amazon EC2)
In cluster definition we use key word _ec2_ for deploying the cluster on Amazon EC2 Cloud.  Following code snippet shows all supported attributes for AWS.

 ```yaml
ec2:
  type: c4.large
  region: eu-west-1
  ami: ami-47a23a30
  price: 0.1
  vpc: vpc-f70ea392
  subnet: subnet-e7830290
```

<a href="http://aws.amazon.com/ec2/instance-types/">Type of the virtual machine</a>, <a href="http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html">region (data center)</a> and <a href="http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html">Amazon Machine Image</a> are the basic properties.

We support <a href="http://aws.amazon.com/ec2/purchasing-options/spot-instances/">spot instances</a> that is a way to control your budget. Since Amazon prices are changing based on demand, price is a limit you can set if you are not willing to pay beyond that limit (price unit is USD).  

#####Virtual Private Cloud on AWS-EC2
We support <a href="http://aws.amazon.com/vpc/">AWS VPC</a> on EC2 for better performance. First you must define your VPC in EC2 with the following steps then include your vpc and subnet id in the cluster definition as it is shown above.  

0. Make a VPC and a subnet assigned to it under your ec2.
1. Check the "Auto-assign Public IP" item for your subnet. 
2. Make an internet gateway and attach it to the VPC.
3. Make a routing table for your VPC and add a row for your gateway into it, on this row open all ips '0.0.0.0/0'.
4. Add your vpc-id and subnet-id into the ec2 section of your yaml like the following example. Also make sure you are using the right image and type of instance for your vpc. 


###Web UI
---
Karamel provides a web-ui, built on AngularJS.


###Developers Guide
---
We have organized our code into two main projects, _karamel-core_ and _karamel-ui_. The core is our engine for launching, installing and monitoring clusters. The UI is a standalone web application containing several designers and visualizers. There is a REST-API in between the UI and the core.

The core and REST-API are programmed in Java 7, and the UI is programmed in <a href="https://angularjs.org/">Angular JS<a>.  

####Code quality 
1. Testability and mockability: Write your code in a way that you test each unit separately. Split concerns into different modules that you can mock one when testing the other. We use JUnit-4 for unit testing and <a href="http://mockito.org/">mockito</a> for mocking. 
2. Code styles: Write a DRY (Don't repeat yourself) code, use spaces instead of tab and line width limit is 120. 
3. We use <a href="https://code.google.com/p/guava-libraries/wiki/GuavaExplained">Google Guava</a> and its best practices, specially the basic ones such as nullity checks and preconditions. 

####Build and run from Source
Ubuntu Requirements:
```{r, engine='sh'}
apt-get install lib32z1 lib32ncurses5 lib32bz2-1.0
```
Centos 7 Requirements:
```{r, engine='sh'}
Install zlib.i686, ncurses-libs.i686, and bzip2-libs.i686 on CentOS 7
```
Building from root directory:
```{r, engine='sh'}
mvn install 
```
Running:
```{r, engine='sh'}
cd karamel-ui/target/appassembler
./bin/karamel
```


###Building Window Executables
You need to have 32-bit libraries to build the windows exe from Linux, as the launch4j plugin requires them.

```{r, engine='sh'}
sudo apt-get install gcc binutils-mingw-w64-x86-64 -y
# Then replace 32-bit libraries with their 64-bit equivalents
cd /home/ubuntu/.m2/repository/net/sf/launch4j/launch4j/3.8.0/launch4j-3.8.0-workdir-linux/bin
rm ld windres
ln -s /usr/bin/x86_64-w64-mingw32-ld ./ld
ln -s /usr/bin/x86_64-w64-mingw32-windres ./windres
```
Then run maven with the -Pwin to run the plugin:
```{r, engine='sh'}
mvn -Dwin package
```
