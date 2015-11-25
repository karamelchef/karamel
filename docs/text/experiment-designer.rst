.. _experiment-designer:

Experiment Designer
-------------------

Experiment Designer in Karamel helps you to design your experiment in bash script or phyton without needing to know Chef or Git. Take the following steps to design and deploy your experiment.

Find experiment designer
````````````````````````
  When you have Karamel web app up and running, you can access the experiment designer from the Experiment menu-item on the left-hand side of the application.

  .. figure:: ../imgs/exp1.png
     :alt: Get into the experiment designer.
     :scale: 100
     :figclass: align-center	  

     Get into the experiment designer.

Login into Github
`````````````````
  Github is Karamel's artifact server, here you will have to login into your Github account for the first time while Karamel will remember your credentials for other times.

  .. figure:: ../imgs/exp2.png
     :alt: Login button.
     :scale: 100
     :figclass: align-center	  

     Login button.

  .. figure:: ../imgs/exp3.png
     :alt: Github credentials.
     :scale: 100
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
  At this point you land into the programming section of your experiment. By default in Karamel your experiment's name will be take as cookbook's name and the default experiment recipe is called "experiment". In the large text-are, as you observe in the below screenshot, you can write your experiment code either in bash or phyton, Karamel will wrap your code into a chef code. All parameters in experiment come in the format of Chef variables, you should wrap them inside #{} - Chef populates them at runtime. If you write results of your experiment in a file called /tmp/wordcount_experiment.out - if your cookbook called "wordcount" and your recipe called "experiment"- Karamel will download that file and will put it into ~/.karamel/results/ folder for your further consideration.

  .. figure:: ../imgs/exp6.png
     :alt: Experiment's script.
     :scale: 100
     :figclass: align-center	  

     Experiment's script.


Define orchestration rules for experiment
`````````````````````````````````````````
  Placing your experiment in the right order in the cluster orchestration is very essential part of your experiment design. For that just mark the advance check-mark and specify to which other recipes in the cluster your experiment is dependent. After you locate all the dependencies you must give the right reference to the cookbook address in the second text-area.

  .. figure:: ../imgs/exp7.png
     :alt: Orchestration rules for new cluster.
     :scale: 100
     :figclass: align-center	  

     Orchestration rules for new cluster.

Push your experiment into Github
````````````````````````````````
  In the end by pressing the save button your cookbook will be generated and will be copied into Github.

  .. figure:: ../imgs/exp8.png
     :alt: Push the experiment into Github.
     :scale: 100
     :figclass: align-center	  

     Push the experiment into Github.


Approve uploaded experiment into Github
```````````````````````````````````````
Look into your Github repo you can see your cookbook.

  .. figure:: ../imgs/exp9.png
     :alt: New experiment landed into Github.
     :scale: 100
     :figclass: align-center	  

     New experiment landed into Github.
