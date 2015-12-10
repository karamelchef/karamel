.. _experiment-designer:

Experiment Designer
-------------------

The experiment Designer perspective in Karamel helps you to design your experiment in a bash script or a python program without needing to know Chef or Git. Take the following steps to design and deploy your experiment.

Find experiment designer
````````````````````````
When you have Karamel web app up and running, you can access the experiment designer from the Experiment menu-item on the left-hand side of the application.

  .. figure:: ../imgs/exp1.png
     :alt: Get into the experiment designer.
     :scale: 75
     :figclass: align-center	  

     Get into the experiment designer.

Login into Github
`````````````````
Github is Karamel's artifact server, here you will have to login into your Github account for the first time while Karamel will remember your credentials for other times.

  .. figure:: ../imgs/exp2.png
     :alt: Login button.
     :scale: 75
     :figclass: align-center	  

     Login button.

  .. figure:: ../imgs/exp3.png
     :alt: Github credentials.
     :scale: 60
     :figclass: align-center	  

     Github credentials.

Start working on experiment
```````````````````````````
You can either create a new experiment or alternatively load the already designed experiment into the designer.

  .. figure:: ../imgs/exp4.png
     :alt: Work on a new or old experiment.
     :scale: 100
     :figclass: align-center	  

     Work on a new or old experiment.

Create a new experiment
```````````````````````
If you choose to create a new experiment you will need to choose a name for it, optionally describe it and choose which Github repo you want to host your experiment in. As you can see in the below image Karamel connects and fetches your available repos from Github.

  .. figure:: ../imgs/exp5.png
     :alt: New experiment on a Github repo.
     :scale: 100
     :figclass: align-center	  

     New experiment on a Github repo.

Write body of experiment
````````````````````````
At this point you land into the programming section of your experiment. The default name for the experiment recipe is "experiment". In the large text-area, as can be seen in the screenshot below, you can write your experiment code either in bash or python. Karamel will automatically wrap your code into a chef recipe. All parameters in experiment come in the format of Chef variables, you should wrap them inside #{} and prefix them ``node.<cookbookname>``. By default, they have the format ``#{node.cookbook.paramName}``, where ``paramName`` is the name of your parameter. If you write results of your experiment in a file called /tmp/wordcout__experiment.out - if your cookbook called "wordcount" and your recipe called "experiment"- Karamel will download that file and will put it into ~/.karamel/results/ folder of your client machine.

  .. figure:: ../imgs/exp6.png
     :alt: Experiment's script.
     :scale: 100
     :figclass: align-center	  

     Experiment bash script.


Define orchestration rules for experiment
`````````````````````````````````````````
Placing your experiment in the right order in the cluster orchestration is a very essential part of your experiment design. Click the ``advanced`` checkbox, write in the line-separated Cookbook::recipe_name that your experiment requires have finished before the experiment will start. If your experiment is dependent on other cookbooks (for recipes or parameters), you must enter the relative GitHub name for the cookbook and the version/branch in line-separated format in the second text-area.

  .. figure:: ../imgs/exp7.png
     :alt: Orchestration rules for new cluster.
     :scale: 100
     :figclass: align-center	  

     Orchestration rules for new cluster.

Push your experiment into Github
````````````````````````````````
You can save your cluster to GitHub by pressing the save button in the top-right hand corner of the webpage. This will generate your cookbook and copy all the files to Github by adding, committing, and pushing the new files to GitHub.

  .. figure:: ../imgs/exp8.png
     :alt: Push the experiment into Github.
     :scale: 100
     :figclass: align-center	  

     Push the experiment to a Github repository.


Approve uploaded experiment to Github
```````````````````````````````````````
Navigate to your Github repo on your web browser and you can see your cookbook.

  .. figure:: ../imgs/exp9.png
     :alt: New experiment landed into Github.
     :scale: 90
     :figclass: align-center	  

     New experiment added to Github.
