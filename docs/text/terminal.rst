.. _karamel-terminal:

Karamel Terminal
----------------
The terminal perspective enables user to monitor and manage running clusters as well as debugging Chef recipes by running them. 

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
The Terminal has a menu bar in which the available menu items (buttons) change dynamically based on the active page. 

Command Bar
```````````
Under the menu bar, there is a long text area where you can execute commands directly. The buttons (menu items) are, in fact, just widely used commands. To see list of commands click on the Help button. 
  
Main Page
`````````
The main page in the terminal shows available running clusters - you can run multiple clusters at the same time they just need to have different names - where you see general status of your cluster. There are some actions in front of each cluster where you can obtain more detail information about each cluster. 

    .. figure:: ../imgs/terminal1.png
     :alt: Successful launch redirects to terminal page.
     :figclass: align-center
     :scale: 80
     
     Successful launch redirects to terminal page.

Cluster Status
``````````````
Status page pushes the new status for the chosen cluster very often. In the first table you see phases of the cluster and each of them they passed successfully or not. 

  .. figure:: ../imgs/terminal2.png
     :alt: Cluster Status - A just started cluster
     :figclass: align-center
     :scale: 80

     Cluster Status - A recently started cluster

  
The cluster deployment phases are:
    #1. Pre-Cleaning
    #2. Forking Groups
    #3. Forking Machines
    #4. Installing

As soon as the cluster passes the forking groups phase, a list of machine tables appear under the phases table. Each machine table indicates that the virtual machine (VM) has been forked and some details on the VM are available,  such as its IP Addresses (public and private) and its connection status. 
  
Inside each machine table there exists a smaller table for showing the tasks that are going to be submitted into that machine. Before all machines became forked and ready, all task tables are empty for all machines.

  .. figure:: ../imgs/terminal3.png
     :alt: Cluster Status - Forking Machines
     :figclass: align-center
     :scale: 80
     
     Cluster Status - Forking Machines
  
Once all machines have started forking tasks, a list of tasks are displayed for each machine. The Karamel Scheduler orders tasks and decides when each task is ready to be run. The scheduler assigns a status label to each task.
  
The task status labels are:
    * Waiting: the task is still waiting until its dependencies have finished;
    * Ready: the task is ready to be run but the associated machine has not yet taken it yet because it is running another task;
    * Ongoing: the task is currently running on the machine;
    * Succeed: the task has finished successfully;
    * Failed: the task has failed - each failure will be propagated up into cluster and will cause the cluster to pause the installation.

When a task is finished a link to its log content will be displayed in the third column of task table. The log is the merged content of the standard output and standard error streams. 

  .. figure:: ../imgs/terminal4.png
     :alt: Cluster Status - Installing
     :figclass: align-center
     :scale: 80

     Cluster Status - Installing.


Orchestartion DAG
`````````````````
The scheduler in Karamel builds a Directed Acyclic Graph (DAG) from the set of tasks in the cluster. In the terminal perspective, the progress of the DAG execution can be visualized by clicking on the "Orchestration DAG" button. 

Each Node of the DAG represents a task that must be run on a certain machine. Nodes dynamically change their color according to the status change of their tasks. Each color is interpreted as follows:

    * Blue: Waiting
    * Ready: Yellow
    * Ongoing: Blinking orange
    * Succeed: Green
    * Failed: Red

  .. figure:: ../imgs/terminal5.png
     :alt: Orchestration DAG
     :figclass: align-center
     :scale: 80
     
     Orchestration DAG


The Orchestration DAG is not only useful to visualize the cluster progress but can also help in debugging the level of parallelization in the installation graph. If some tasks are acting as global barriers during installation, they can be quickly identified by inspecting the DAG and seeing the nodes with lots of incoming edges and some outgoing edges. As have local orchestration rules in their Karamelfiles, the DAG is built from the set of Karamelfiles. It is not easy to manually traverse the DAG, given a set of Karamelfiles, but the visual DAG enables easier inspection of the global order of installation of tasks.

Quick Links
```````````
Quick links a facility that Karamel provides in the terminal perspective to access web pages for services in your cluster. For example, when you install Apache Hadoop, you might want to access the NameNode or ResourceManager's web UI. Those links must :ref:`be designed <write_quick_links>` in karamelized cookbooks (in the ``metadata.rb`` file). Karamel parses the ``metadata.rb`` files, extracting the webpage links and displaying them in the *Quick Links* tab. 

  .. figure:: ../imgs/terminal6.png
     :alt: Quick Links
     :figclass: align-center
     :scale: 80
     
     Quick Links


Statistics
``````````
Currently Karamel collects information about the duration of all tasks when you deploy a cluster. Duration statistics are available by clicking on statistics button that will show the names of the tasks and their execution time. It might be have you have several instances of each task in your cluster, for example, you may install the ``hadoop::dn`` recipe on several machines in your cluster - all such instances will appear in the statistics table. Statistics is a good way for performance measurement for some type of experiments. You can just draw a plot on them to show the performance of your experiment.

Pause/Resume
````````````
A cluster may pause running either because the user's order or when a failure happens. It is a good way if user wants to change something or if he wants to avoid running the entire cluster for some reason. In that case when you click on the "Pause" button it takes some time until all machines finish their current running task and go into the paused mode. When cluster is paused, a resume button will appear which proceeds running the cluster again.

Purge
`````
Purge is a button to destroy and release all the resources both on Clouds and Karamel-runtime, destroying any virtual machines created. It is recommended to use the purge function via Karamel to clean-up resources rather than doing so manually - Karamel makes sure all ssh connections, local threads, virtual machines and security groups are released completely. 
