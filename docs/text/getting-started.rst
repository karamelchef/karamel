: _getting-started:

Getting Started
===============

To run a simple cluster you need the have: 
  * a :ref:`cluster definition <cluster-definition>` file 
  * access to the cloud-provider of your choice
  * Karamel system up and running. 

You can use Karamel standalone web application on your machine or alternatively use our Java-API to start your cluster. 

Web-UI:
~~~~~~~
To run your cluster with our Web-Application take the following steps:

**1. Download Binary** 
  Download Karamel binary from github, unzip it and access its folder through your machine's terminal and run this command:: 

    .. code-block:: bash

      cd karamel-0.x
      ./bin/karamel


**2. Load Cluster Definition** 
  It opens the Web-UI in your browser. From the top-left most menu-items, open your cluster definition. 

**3. Board-UI: Modify Cluster Definition**
  Some columns are appearing in the board perspective of the Web-UI, each column represents a group of machines that are similar in terms of software stack. 

  On top of each column you see name of the group following with number of machines in that group in a parenthesis. There is a set of cards piled into each column, each card shows a service or a software routine associated with a Chef-Recipe. 

  In the board view you have the possibility to modify your group structure and definitions such as add/remove/edit for group/recipe/cloud-provider and etc.


**4. Fine-tune Attributes**
  When you are done with your cluster structure you can further customize your cluster through Configure button in the middle of the top bar. By choosing that a configuration dialog will pop up, there you see several tabs each named after one used chef-cookbook in the cluster definition. Those attributes are pre-built by cookbook designers for run-time customization. There are two types of attributes mandatory and optional - most of them usually have a default value but if they don't user must fill in some values for them before proceed. 

  By default each cookbook has a parameter for operating system's user-name and group-name. It is recommended to set the same user and group for all cookbooks that you don't face with permission issues. 

  It is also important to fine-tune your systems with the right parameters, for instance according to type of the machines you wanna use for your cluster you should allocate enough memory to each system. 


**5. Give and Approve Credentials** 
  Finally you have to launch your cluster by pressing launch icon in the top bar. There exist a few tabs that user must go through all of them, you might have to specify values and confirm everything. Even though Karamel caches those values, you have to always confirm that Karamel is allowed to use those values for running your cluster.

  In this step first you need to specify your ssh key pair - Karamel uses that to establish a secure connection to virtual machines. For Linux and Mac operating systems, Karamel finds the default ssh key pair in your operating system and will use that unless you want to change it. In that case you can just check the advance box and from there ask Karamel to generate a new key pair for you. When your ssh key is password-protected you need to enter your password in the provided box, and also in case you use bare-metal (karamel doesn't fork machines from cloud) you have to give sudo-account access to your machines. 

  In the second step you need to give credentials for accessing the cloud of your choice. If your cluster is running on a single cloud a tab related to that cloud will appear in the launch dialog and if you use multi-cloud a separate tab for each cloud will appear. Credentials are usually in different formats for each cloud, for more detail information please find it in the related cloud section. 


**6. Launch**
  When you have all the steps passed in the summary tab you can launch your cluster, it will bring you to the :ref:`terminal <karamel-terminal>` there you can control the installation of your cluster.


Java-API:
~~~~~~~~~
You can run your cluster in your Java program by using our API.

**1. Jar-file dependency**
  First add a dependency into the karamel-core jar-file, its pom file dependency is as following:
  
    .. code-block:: xml

      <dependency>
        <groupId>se.kth</groupId>
        <artifactId>karamel-core</artifactId>
        <scope>compile</scope>
      </dependency>

**2. Call KaramelApi**
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

  The code prints out your cluster status in the console every minute. 