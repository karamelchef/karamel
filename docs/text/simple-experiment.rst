This experiment is a wordcount program for MapReduce that takes as a parameter an input textfile as a URL. The program counts the number of occurances of each word found in the input file.
First, create a new experiment called `mapred` in GitHub (any organization). You will then need to click on the ``advanced`` tickbox to allow us to specify dependencies and parameters.
.. We keep them separate experiments to measure their time individually. 

.. A) Random Text Generator

.. The following code snippet generates a random text of size 128MB and copies it into HDFS. The filename in HDFS is the fully-qualified hostname of the node that executes the experiment. This avoids naming conflicts in the case where we want several nodes to run the same textfile generation code on many servers in parallel.

..    rm -f /tmp/input.txt
..    base64 /dev/urandom | head -c 128000000 > /tmp/input.txt
..    /srv/hadoop/bin/hdfs dfs -mkdir -p /words
..    /srv/hadoop/bin/hdfs dfs -copyFromLocal /tmp/input.txt /words/#{node.name}

 Parameters  
::::::::::::::::::::::::

  .. code-block:: bash
		  
    user=mapred
    group=mapred
    textfile=http://www.gutenberg.org/cache/epub/1787/pg1787.txt


  .. figure:: ../imgs/mapred-wc-params.png
     :alt: Wordcount input parameter URL.
     :figclass: align-center
     :scale: 80

     Defining the texfile input parameter. Parameters are key-value pairs defined in the Parameter box.
	      
    
    
A-1) Dependent Recipes (Karamelfile)
'''''''''''''''''''''''''''''''''''''''''''
The code generator bash script must wait until all HDFS datanodes and YARN nodemanagers are up and running before it is run. To indicate this, we add the following lines to Dependent Recipes textbox:

  .. code-block:: bash

    hadoop::dn
    hadoop::nm		  
    
A-2) Cookbook Dependencies (Berksfile)
'''''''''''''''''''''''''''''''''''''''''''''
Our new cookbook will be dependent on the hadoop cookbook, and we have to enter into the ``Cookbook Dependencies`` textbox the relative path to the cookbook on GitHub:

  .. code-block:: bash

    cookbook 'hadoop', github: 'hopshadoop/apache-hadoop-chef'


  .. figure:: ../imgs/mapred-wc-orchestration.png
     :alt: Wordcount orchestration.
     :figclass: align-center
     :scale: 80

     Define the Chef ``cookbook dependencies`` as well as the ``dependent recipes``, the recipes that have to start before the experiments in this cookbook.

    
B) Experiment: Word-count on MapReduce 
::::::::::::::::::::::::::::::::::::::::::
The following code snippet runs MapReduce wordcount on the input parameter ``textfile``. The parameter is referenced in the bash script as ``#{node.mapred.textfile}``, which is a combination of ``node``.``<cookbookname>``.``<parameter>``.
  
..  As you you can see below, the parallelization parameter (``-p #{node.flink.taskmanager.private_ips.size}``) is the number of flink taskmanagers, which is, in turn, depend depends on size of your cluster. Namenode address will be also binded at runtime. 

  .. code-block:: bash

    # This line downloads the input text file, entered as a URL
    cd /tmp &&  wget #{node.mapred.textfile} -o /tmp/input.txt
		  cd /srv/hadoop
    # This line runs the wordcount MapReduce program on the input text files and saves the output containing the word counts in HDFS
    ./bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.1.jar file:///tmp/input.txt hdfs://#{node.hadoop.nn.private_ips[0]}:8020/User/mapred/counts
    # This lines copies the counts textfile to a special file that will be downloaded to the Karamel client machine when the recipe exits successfully.
    ./bin/hdfs dfs -copyToLocal hdfs://#{node.hadoop.nn.private_ips[0]}:8020/User/mapred/counts file:///tmp/mapred__experiment.out

  
  .. figure:: ../imgs/mapred-wc-experiment.png
     :alt: Wordcount experiment.
     :figclass: align-center
     :scale: 80

     The wordcount experiment is a bash script that downloads the input textfile, runs the wordcount MapReduce program on the file, saving the output to HDFS, and finally downloads the results to the Karmel client application.
