Experiment Designer in Karamel helps you to design your experiment in bash script or phyton without needing to know Chef or Git. Take the following steps to design and deploy your experiment.

1. When you have Karamel web app up and running, you can access the experiment designer from the Experiment menu-item on the left-hand side of the application.

  .. image:: ../imgs/exp1.png
      :align: center

2. Github is Karamel's artifact server, here you will have to login into your Github account for the first time while Karamel will remember your credentials for other times.

  .. image:: ../imgs/exp2.png
      :align: center

  .. image:: ../imgs/exp3.png
      :align: center

3.You can either create a new experiment or alternatively load the already designed experiment into the designer.

  .. image:: ../imgs/exp4.png
      :align: center


4. If you choose to create a new experiment you will need to choose a name for it, optionally describe it and choose which Github repo you want to host your experiment in. As you can see in the below image Karamel connects and fetches your available repos from Github.

  .. image:: ../imgs/exp5.png
      :align: center


5. At this point you land into the programming section of your experiment. By default in Karamel your experiment's name will be take as cookbook's name and the default experiment recipe is called "experiment". In the large text-are, as you observe in the below screenshot, you can write your experiment code either in bash or phyton, Karamel will wrap your code into a chef code. All parameters in experiment come in the format of Chef variables, you should wrap them inside #{} - Chef populates them at runtime. If you write results of your experiment in a file called /tmp/wordcount_experiment.out - if your cookbook called "wordcount" and your recipe called "experiment"- Karamel will download that file and will put it into ~/.karamel/results/ folder for your further consideration.

  .. image:: ../imgs/exp6.png
      :align: center


6. Placing your experiment in the right order in the cluster orchestration is very essential part of your experiment design. For that just mark the advance check-mark and specify to which other recipes in the cluster your experiment is dependent. After you locate all the dependencies you must give the right reference to the cookbook address in the second text-area.

  .. image:: ../imgs/exp7.png
      :align: center


7. In the end by pressing the save button your cookbook will be generated and will be copied into Github.

  .. image:: ../imgs/exp8.png
      :align: center


8. Look into your Github repo you can see your cookbook.

  .. image:: ../imgs/exp9.png
      :align: center
