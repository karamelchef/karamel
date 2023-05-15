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

#### Recipes execution parallelism
Karamel will execute recipes in parallel on all machines respecting any dependency stated in Karamelfile. Regardless 
of the dependencies, the same recipes will be executed in parallel on all machines. For example if we run recipe 
`ndb::ndbd` on three machines, they may be executed in parallel depending on how Karamel will schedule them.

In some cases we want to guarantee only a portion of machines to execute recipes in parallel. For example in case of 
a rolling restart/upgrade we want to execute `ndb::ndbd` serially. You can configure recipe parallelism under 
`runtimeConfiguration/recipesParallelism` section in the cluster definition in the format `recipe_name: parallelism`.
Multiple recipes can be configured. For example:

```yaml
runtimeConfiguration:                                                                                                                                                                                                                         
  recipesParallelism:                                                                                                                                                                                                                         
    consul::master: 1                                                                                                                                                                                                                         
    ndb::ndbd: 1                                                                                                                                                                                                                         
    ndb::mysqld: 2 
    ndb::mysqld_tls: 2 
    onlinefs::default: 1 
    hops::nn: 1                                                                                                                                                                                                                               
    hops::rm: 1
```

#### Cloud providers
We support five cloud providers: Amazon EC2 (ec2), Google Compute Engine (gce), Openstack Nova (nova), OCCI and bare-metal (baremetal). You can define provider globally or per group. In the group scope, you can overwrite some attributes of the network/machines in the global scope or you can entirely choose another cloud provider, that's how we support multi-cloud deployment. Settings and properties for each provider is introduced in a following separate section. 

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

#### AWS(Amazon EC2)
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

##### Virtual Private Cloud on AWS-EC2
We support <a href="http://aws.amazon.com/vpc/">AWS VPC</a> on EC2 for better performance. First you must define your VPC in EC2 with the following steps then include your vpc and subnet id in the cluster definition as it is shown above.  

0. Make a VPC and a subnet assigned to it under your ec2.
1. Check the "Auto-assign Public IP" item for your subnet. 
2. Make an internet gateway and attach it to the VPC.
3. Make a routing table for your VPC and add a row for your gateway into it, on this row open all ips '0.0.0.0/0'.
4. Add your vpc-id and subnet-id into the ec2 section of your yaml like the following example. Also make sure you are using the right image and type of instance for your vpc. 

#### Openstack

If you want to use your private Openstack cloud infrastructure, you need to make use of the keyword _nova_ in the 
cluster definition file. In this case, the syntax and keywords for some of the terms change compared to other providers.

_Flavor_ corresponds to the type of instance in terms of HW and resources you have configured, in this case this value
needs to match to one of your configured flavors and it needs to be the flavor id.

_image_ corresponds to the image it will use on the VM and it corresponds to the image id you can get from the uploaded 
images list, which usually is a hashed id value in your list of available images.

Here is an example of an Openstack yaml configuration:

 ```yaml
nova:
  flavor: 3
  image: "99f4625e-f425-472d-8e21-7aa9c3db1c3e"
```

##### Using Openstack credentials

Before deploying your cluster, you will have the option of configuring your credentials. For this provider, you will need
to retrieve the following:

- __Keystone Endpoint:__ HTTP url to your openstack system, usually it is a URL with the form http://xxx.xxx.xxx.xxx:5000/v2
- __Openstack Region:__ The name of the region where your have access to you tenancy access.
- __Account Pass:__ Your password for your user.
- __Account Name:__ Your access name with your corresponding tenant access, in the following form "tenantSpace:TenantName".
In most cases, the tenantSpace can corresponding to your project, so if your project is __"HOPS"__, and the username 
is __"flink"__, then the account name is _"HOPS:flink"_

#### Google Compute Engine
To deploy the cluster on Google’s infrastructure, we use the keyword _gce_ in the cluster definition YAML file. Following code snippet shows the current supported attributes:
 ```yaml
gce:
  type: n1-standard-1
  zone: europe-west1-b
  image: ubuntu-1404-trusty-v20150316
  vpc: default
  subnet: default
  diskSize: 15
  nvme: 1
  hdd: 1
  ssd: 1
```
<a href="https://cloud.google.com/compute/docs/machine-types">Machine type</a>, <a href="https://cloud.google.com/compute/docs/zones">zone of the VMs</a>, <a href="https://cloud.google.com/vpc/docs/vpc">VPC network</a>, <a href="https://cloud.google.com/compute/docs/images">VM image</a>, and the diskSize (in GB) can be specified by the user. A user can also specify the number of <a href="https://cloud.google.com/compute/docs/disks/">HDD, SSD, and NVMe </a> disks to be attached to the machine. The diskSize attribute specifies the diskSize for the boot disk and any HDD or SSD disks. The diskSize for the NVMe is 375 GB by default and cann't be changed.

First of all, you need to enable Google Compute Engine API by going to the API Manager section in your Google Cloud Platform account. Karamel uses Compute Engine’s OAuth 2.0 authentication method. Therefore, an OAuth 2.0 client ID needs to be created through the Google’s Developer Console. The description on how to generate a client ID is available <a href="https://support.google.com/cloud/answer/6158849?hl=en">here</a>. You need to select _Service account_ as the application type. After generating a service account, click on _Generate new JSON key_ button to download a generated JSON file that contains both private and public keys. You need to register the fullpath of the generated JSON file with Karamel API.

#### Bare-metal
In case of bare-metal muchies must be ready before hand and their ip-address are specified in the cluster definition. If you have many ip-addresses in a range, it is possible to give range of addresses instead of specifying them one by one (second following exmaple). Machines' credentials with super-user previlledges are required for establishing ssh connection. The username goes into the cluster definition while the sudo-password must be registered through our API.   
 ```yaml
baremetal:
  username: ubuntu
  ips: 
   - 192.168.33.12
   - 192.168.33.13
   - 192.168.33.14
   - 192.168.44.15
```
IP-Range:  
 ```yaml
baremetal:
  username: ubuntu
  ips: 
   - 192.168.33.12-192.168.33.14
   - 192.168.44.15
```

#### OCCI
To deploy the cluster on OCCI compatible infrastructure you must provide following information via yaml file:
 - occiEndpoint - defines on which OCCI cluster will your virtual machines be created
 - occiImage - virtual machine template
 - occiImageSize - compute resource size (procesors & memory)
 - usename - account name with root privileges

   
 ```yaml
name: OcciAmbari
occi:
  occiEndpoint: "https://carach5.ics.muni.cz:11443"
  occiImage: "http://occi.carach5.ics.muni.cz/occi/infrastructure/os_tpl#uuid_egi_ubuntu_server_14_04_lts_fedcloud_warg_131"
  occiImageSize: "http://fedcloud.egi.eu/occi/compute/flavour/1.0#mem_large"
  usename: "ubuntu"

cookbooks:
  ambari:
    github: "jimdowling/ambari"
    branch: "master"

groups:
  server:
    size: 1
    recipes:
        - ambari::server
  agents:
    size: 1
    recipes:
        - ambari::agent
```


### Web UI
---
Karamel provides a web-ui, built on AngularJS.


### Developers Guide
---
We have organized our code into two main projects, _karamel-core_ and _karamel-ui_. The core is our engine for launching, installing and monitoring clusters. The UI is a standalone web application containing several designers and visualizers. There is a REST-API in between the UI and the core.

The core and REST-API are programmed in Java 7, and the UI is programmed in <a href="https://angularjs.org/">Angular JS<a>.  

#### Code quality 
1. Testability and mockability: Write your code in a way that you test each unit separately. Split concerns into different modules that you can mock one when testing the other. We use JUnit-4 for unit testing and <a href="http://mockito.org/">mockito</a> for mocking. 
2. Code styles: Write a DRY (Don't repeat yourself) code, use spaces instead of tab and line width limit is 120. 
3. We use <a href="https://code.google.com/p/guava-libraries/wiki/GuavaExplained">Google Guava</a> and its best practices, specially the basic ones such as nullity checks and preconditions. 

#### Build and run from Source
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


#### Building Window Executables
You need to have 32-bit libraries to build the windows exe from Linux, as the launch4j plugin requires them.

```{r, engine='sh'}
sudo apt-get install gcc binutils-mingw-w64-x86-64 -y
# Then replace 32-bit libraries with their 64-bit equivalents
cd /home/$USER/.m2/repository/net/sf/launch4j/launch4j/3.9/launch4j-3.9-workdir-linux/bin
rm ld windres
cp /usr/bin/x86_64-w64-mingw32-ld ./ld
cp /usr/bin/x86_64-w64-mingw32-windres ./windres
```
Then run maven with the -Pwin to run the plugin:
```{r, engine='sh'}
mvn -Dwin package
```
# Launch4j linux 64 bug report:
# https://github.com/lukaszlenart/launch4j-maven-plugin/issues/29
