.. _board-ui:

Board-UI
--------
Board is the main page of our web-app as soon as you run Karamel. It is a semi-visual representation of your cluster definition where you can modify your cluster definition and run it.

Load Cluster Definition
```````````````````````
  From the top-left menu-items, open your cluster definition. 

  .. image:: ../imgs/board1.png
      :align: center

  Some columns are appearing in the board perspective of the Web-UI, each column represents a group of machines that are similar in terms of software stack. On top of each column you see name of the group following with number of machines in that group in a parenthesis. There is a set of cards piled into each column, each card shows a service or a software routine associated with a Chef-Recipe. 

  .. image:: ../imgs/board2.png
      :align: center

Change group name and size
``````````````````````````
  For changing name and number of machines in each group double click on the header of each group, in the following dialog do your changes and submit it.

  .. image:: ../imgs/board3.png
      :align: center

Add new recipe into group
`````````````````````````
  In the top-left icon in the header of each group, there is a menu to change group detail, choose add recipe in that group:
  
  .. image:: ../imgs/board4.png
      :align: center

  For adding a recipe you must have the github address of a Karamelzied Cookbook where your recipe is located, it is easy if you copy the url from your browser and paste it in the Url field and press fetch to load available recipes for you. Choose your recipe from the below combo-box.
  
  .. image:: ../imgs/board5.png
      :align: center

Customize Chef attributes for group
```````````````````````````````````
  Chef attributes are customizable in the group scope - group scope values precede over cluster scope values. To do so, select its menu item from group settings menu.
 
  .. image:: ../imgs/board6.png
      :align: center

  In the appearing dialog, there is a tab per used cookbook in that group, in each tab you see all customizable attributes, some of them are mandatory and some optional with some default values. User must set value for all the mandatory attributes that don't have any value already.
 
  .. image:: ../imgs/board7.png
      :align: center

Customize cloud provider for group
``````````````````````````````````
  In your cluster you can have different settings for cloud provider inside each group - that's how we support multi-cloud deployments. Like attributes, group scope cloud settings will override the global scope. Should you have multi-cloud settings in in your cluster, at the launch time you must supply credentials for each cloud separately in the launch dialog.
  
  .. image:: ../imgs/board8.png
      :align: center

  Choose the provider for the current group then you will see detail settings for that. 

  .. image:: ../imgs/board9.png
      :align: center

Delete group
````````````
  If you want to delete a group find the menu-item in the group menu. 
  
  .. image:: ../imgs/board10.png
      :align: center

  Once you delete a group the column and all the settings related to that group will be disappeared forever.  
  
  .. image:: ../imgs/board11.png
      :align: center

Fine-tune cluster scope attributes
``````````````````````````````````
  When you are done with your group settings you can have some global values for Chef attributes. By choosing Configure button in the middle of the top bar a configuration dialog will pop up, there you see several tabs each named after one used chef-cookbook in the cluster definition. Those attributes are pre-built by cookbook designers for run-time customization. There are two types of attributes mandatory and optional - most of them usually have a default value but if they don't user must fill in some values for them before proceed. 

  .. image:: ../imgs/board12.png
        :align: center

  By default each cookbook has a parameter for operating system's user-name and group-name. It is recommended to set the same user and group for all cookbooks that you don't face with permission issues. 

  It is also important to fine-tune your systems with the right parameters, for instance according to type of the machines in your cluster you should allocate enough memory to each system. 

  .. image:: ../imgs/board13.png
        :align: center

Start to Launch Cluster
```````````````````````
  Finally you have to launch your cluster by pressing launch icon in the top bar. There exist a few tabs that user must go through all of them, you might have to specify values and confirm everything. Even though Karamel caches those values, you have to always confirm that Karamel is allowed to use those values for running your cluster.

  .. image:: ../imgs/board14.png
      :align: center

Set SSH Keys
````````````
  In this step first you need to specify your ssh key pair - Karamel uses that to establish a secure connection to virtual machines. For Linux and Mac operating systems, Karamel finds the default ssh key pair in your operating system and will use it.
  
  .. image:: ../imgs/board15.png
      :align: center

Generate SSH Key
````````````````
  If you want to change the default ssh-key you can just check the advance box and from there ask Karamel to generate a new key pair for you. 

Password Protected SSH Keys
```````````````````````````
  If your ssh key is password-protected you need to enter your password in the provided box, and also in case you use bare-metal (karamel doesn't fork machines from cloud) you have to give sudo-account access to your machines. 

  .. image:: ../imgs/board16.png
      :align: center

Cloud Provider Credentials
``````````````````````````
In the second step of launch you need to give credentials for accessing the cloud of your choice. If your cluster is running on a single cloud a tab related to that cloud will appear in the launch dialog and if you use multi-cloud a separate tab for each cloud will appear. Credentials are usually in different formats for each cloud, for more detail information please find it in the related cloud section. 

  .. image:: ../imgs/board17.png
      :align: center


Final Control
`````````````
  When you have all the steps passed in the summary tab you can launch your cluster, it will bring you to the :ref:`terminal <karamel-terminal>` there you can control the installation of your cluster.

  .. image:: ../imgs/board18.png
      :align: center


