.. _karamel-terminal:

Karamel Terminal
----------------
Our terminal enables user to monitor and manage running clusters as well as debugging Chef recipes by running them. 

Open Terminal
`````````````

  The Board-UI redirects to the terminal as soon as a cluster launches. Another way to access the terminal is by clicking on the ``terminal``  menu-item from the menu dropdown list, as shown in the screen-shot below.  

  .. figure:: ../imgs/terminal0.png
     :alt: Selecting the terminal perspective
     :scale: 50
     :figclass: align-center	  

     Selecting the terminal perspective.

		 
Button Bar
``````````
  Terminal has a button bar with blue color on top, its buttons change dynamically based on the content. 

Command Bar
```````````
  Under button bar, there is a long text area where you can write commands to see the desired result - terminal works with commands and buttons are widely used commands. To see list of commands click on the Help button. 
  
Main Page
`````````
  The main page in the terminal shows available running clusters - you can run multiple clusters at the same time they just need to have different names - where you see general status of your cluster. There are some actions in front of each cluster where you can obtain more detail information about each cluster. 

    .. figure:: ../imgs/terminal1.png
     :alt: Successful launch redirects to terminal page.
     :figclass: align-center
     :scale: 100
     
     Successful launch redirects to terminal page.

Cluster Status
``````````````
  Status page pushes the new status for the chosen cluster very often. In the first table you see phases of the cluster and each of them they passed successfully or not. 

  .. figure:: ../imgs/terminal2.png
     :alt: Cluster Status - A just started cluster
     :figclass: align-center
     :scale: 100

     Cluster Status - A just started cluster

  
  Cluster phases are the following:
    * Pre-Cleaning
    * Forking Groups
    * Forking Machines
    * Installing

  As soon as it passes the forking groups phase, a list of machine tables appear under the phases table. Each machine table means that machine is forked and with some detail information such as IP Address and its connection status. 
  
  Inside each machine table there exist a smaller table for showing tasks that are going to be submitted into that machine. Before all machines became forked and ready, all task tables are empty for all machines.

  .. figure:: ../imgs/terminal3.png
     :alt: Cluster Status - Forking Machines
     :figclass: align-center
     :scale: 100
     
     luster Status - Forking Machines
  
  Once all machines forked tasks a bunch of tasks are displayed for each machine. Karamel Scheduler orders tasks and decides when each task is ready to be run. The scheduler assigns a status label to each task.
  
  Task status labels are:
    * Waiting: It means task is still waiting until its dependencies are finished.
    * Ready: It means task is ready to be run while the associated machine has not taken it yet because it is running another task.
    * Ongoing: It means task is currently running by the machine.
    * Succeed: When task finishes successfully.
    * Failed: When task fails in the middle - each failure will be propagated up into cluster and will push the cluster to pause the installation.

  When a task is finished a link to its log content will be displayed in the third column of task table. The log is the merged content of output and error streams. 

  .. figure:: ../imgs/terminal4.png
     :alt: Cluster Status - Installing
     :figclass: align-center
     :scale: 100

     Cluster Status - Installing


Orchestartion DAG
`````````````````
  Scheduler in Karamel makes a Directed Acyclic Graph (DAG) out of the all available tasks in the cluster. Tasks could be installation, configuration or running and experiment. In our terminal there is a possibility to watch cluster progress by clicking on the "Orchestration DAG" button. 

  Each Node of the DAG represents a task that must be run on a certain machine. Nodes dynamically change their color according to the status change of their tasks. Here is the meaning of each color:

    * Blue: Waiting
    * Ready: Yellow
    * Ongoing: Blinking orange
    * Succeed: Green
    * Failed: Red

  .. figure:: ../imgs/terminal5.png
     :alt: Orchestration DAG
     :figclass: align-center
     :scale: 100
     
     Orchestration DAG


  Orchestration DAG is not only useful to see the cluster progress but also to grasp a deeper insight about how efficiently you use your machine resources by having a maximum parallelization factor. Technically speaking when a task has a lot of dependency it becomes a bottleneck to maximize the parallelization. It is hard to know this much detail when you design your system's/experiment's cookbooks.   

Quick Links
```````````
  Quick links a facility that Karamel provides in terminal to access service links of your cluster quickly. For example when you install Apache Hadoop, you might want to have access to NameNode's or DataNode's web-ui. Those links must :ref:`be designed <write_quick_links>` in karamelized cookbooks of Hadoop then Karamel will bind their dynamic links and will display them in terminal. 

  .. figure:: ../imgs/terminal6.png
     :alt: Quick Links
     :figclass: align-center
     :scale: 100
     
     Quick Links


Statistics
``````````
  Currently Karamel collects time duration for all tasks when you run a cluster. Time duration statistics are available by clicking on statistics button, it will show the name of tasks versus their execution time. It might be have you have several instance of each task in your cluster, for example you may install hadoop::dn recipe on several machines in your cluster, consequently all instances will appear in the statistics table. 

  Statistics is a good way for performance measurement for some type of experiments. You can just draw a plot on them for showing performance of your experiment.

Pause/Resume
````````````
  A cluster may pause running either because the user's order or when a failure happens. It is a good way if user wants to change something or if he wants to avoid running the entire cluster for some reason. In that case when you click on the "Pause" button it takes some time until all machines finish their current running task and go into the paused mode. When cluster is paused, a resume button will appear which proceeds running the cluster again.

Purge
`````
  Purge is a button to destroy and release all the machine resources both on Clouds and Karamel-runtime. It is recommended to use purge function via Karamel for clean-up resources rather than manually doing so - Karamel makes sure all ssh connections, local threads, virtual machines and security groups are released completely. 
