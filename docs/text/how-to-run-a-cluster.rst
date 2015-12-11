
To run a simple cluster you need: 
  * a :ref:`cluster definition <cluster-definition>` file;
  * access to a cloud (or bare-metal cluster);
  * the Karamel client application.

You can use Karamel as a standalone application with a Web UI or embed Karamel as a library in your application, using the Java-API to start your cluster. 

Linux/Mac
``````````````

**1. Starting Karamel**

  .. include:: run-web-app.rst

	       
Windows
``````````````
**1. Starting Karamel**

To run Karamel, download the Windows binaries from http://www.karamel.io. You first have to unzip the binaries. From Windows Explorer, navigate to the folder ``karamel-0.2`` (probably in the ``Downloads`` folder) and double-click on ``karamel.exe`` file to start Karamel. 


  .. figure:: ../imgs/karamel-landingpage.png
     :alt: Karamel Landing Page.
     :figclass: align-center
     :scale: 60
	   
     Karamel Homepage. Click on ``Menu`` to load a Cluster Definition file.
	   

**2. Customize and launch your cluster** 
Take a look into the :ref:`Board-UI <board-ui>`.


Command-Line in Linux/Mac
``````````````````````````
You can either set environment variables containing your EC2 credentials or enter them from the console. We recommend you set the environment variables, as shown below.

  .. code-block:: bash
  
    export AWS_KEY=...
    export AWS_SECRET_KEY=...
    ./bin/karamel -launch examples/hadoop.yml

After you launch a cluster from the command-line, the client loops, printing out to stdout the status of the install DAG of Chef recipes every 20 seconds or so. Both the GUI and command-line launchers print out stdout and stderr to log files that can be found from the current working directory in:

  .. code-block:: bash

    tail -f log/karamel.log

Java-API:
`````````
You can run your cluster in your Java program by using our API.

**1. Jar-file dependency**
  First add a dependency into the karamel-core jar-file, its pom file dependency is as following:
  
    .. code-block:: xml

      <dependency>
        <groupId>se.kth</groupId>
        <artifactId>karamel-core</artifactId>
        <scope>compile</scope>
      </dependency>

**2. Karamel Java API**
  Load the content of your cluster definition into a variable and call KaramelApi like this example:
  
    .. code-block:: java

      //instantiate the API
      KaramelApi api = new KaramelApiImpl();

      //load your cluster definition into a java variable
      String clusterDefinition = ...;
      
      //The API works with json, convert the cluster-definition into json
      String json = api.yamlToJson(ymlString);

      //Make sure your ssh keys are available, if not let API generate it for 
      SshKeyPair sshKeys = api.loadSshKeysIfExist("");
      if (sshKeys == null) {
        sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
      }

      //Register your ssh keys, thats the way of confirming your ssh-keys
      api.registerSshKeys(sshKeys);

      //Check if your credentials for AWS (or any other cloud) already exist otherwise register them
      Ec2Credentials credentials = api.loadEc2CredentialsIfExist();
      api.updateEc2CredentialsIfValid(credentials);

      //Now you can start your cluster by giving json representation of your cluster
      api.startCluster(json);

      //You can always check status of your cluster by running the "status" command through the API
      //Run status in some time-intervals to see updates for your cluster
      long ms1 = System.currentTimeMillis();
      int mins = 0;
      while (ms1 + 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
        mins++;
        System.out.println(api.processCommand("status").getResult());
        Thread.currentThread().sleep(60000);
      }

  This code block will print out your cluster status to the console every minute. 
