.. _karamel-terminal:

Karamel Terminal
----------------
Our terminal enables user to monitor and manage running clusters as well as debugging Chef recipes by running them. 

Open Terminal
`````````````

  Board-UI redirects to terminal as soon as a cluster launches. Another way to access the terminal is by clicking on its menu-item in the main top-left menu like the bellow screen-shot.  

  .. image:: ../imgs/terminal0.png
      :align: center


Button Bar
``````````
  Terminal has a button bar with blue color on top, its buttons change dynamically based on the content. 

Command Bar
```````````
  Under button bar, there is a long text area where you can write commands to see the desired result - terminal works with commands and buttons are widely used commands. To see list of commands click on the Help button. 
  
Main Page
`````````
  The main page in the terminal shows available running clusters - you can run multiple clusters at the same time they just need to have different names - where you see general status of your cluster. There are some actions in front of each cluster where you can obtain more detail information about each cluster. 

  .. image:: ../imgs/terminal1.png
      :align: center

Cluster Status
``````````````
  Status page pushes the new status for the chosen cluster very often. In the first table you see phases of the cluster and each of them they passed successfully or not. 

  .. image:: ../imgs/terminal2.png
      :align: center

  Cluster phases are the following:
    * Pre-Cleaning
    * Forking Groups
    * Forking Machines
    * Installing

  As soon as it passes the forking groups phase, a list of machine tables appear under the phases table. Each machine table means that machine is forked and with some detail information such as IP Address and its connection status. 
  
  Inside each machine table there exist a smaller table for showing tasks that are going to be submitted into that machine. Before all machines became forked and ready, all task tables are empty for all machines.

  .. image:: ../imgs/terminal3.png
      :align: center

  Once all machines forked tasks a bunch of tasks are displayed for each machine. Karamel Scheduler orders tasks and decides when each task is ready to be run. The scheduler assigns a status label to each task.
  
  Task status labels are:
    * Waiting: It means task is still waiting until its dependencies are finished.
    * Ready: It means task is ready to be run while the associated machine has not taken it yet because it is running another task.
    * Ongoing: It means task is currently running by the machine.
    * Succeed: When task finishes successfully.
    * Failed: When task fails in the middle - each failure will be propagated up into cluster and will push the cluster to pause the installation.

  When a task is finished a link to its log content will be displayed in the third column of task table. The log is the merged content of output and error streams. 

  .. image:: ../imgs/terminal4.png
      :align: center

Orchestartion DAG
`````````````````
  .. image:: ../imgs/terminal5.png
      :align: center

Quick Links
```````````

  .. image:: ../imgs/terminal6.png
      :align: center
