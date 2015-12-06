This experiment has two parts, input generation and wordcount. We keep them separate experiments to measure their time individually. 

A) Random Text Generator
::::::::::::::::::::::::
The following code snippet generates a random text of size 128MB and copies it into HDFS. Name of the file in HDFS comes after the running node's name to avoid conflicts in case we want several nodes to contribute in text generation for increasing parallelisation.

  .. code-block:: bash

    rm -f /tmp/input.txt
    base64 /dev/urandom | head -c 128000000 > /tmp/input.txt
    /srv/hadoop/bin/hdfs dfs -mkdir -p /words
    /srv/hadoop/bin/hdfs dfs -copyFromLocal /tmp/input.txt /words/#{node.name}

A-1) Orchestration (Karamelfile)
''''''''''''''''''''''''''''''''
The code generator needs all hadoop datanodes to be up and running for having access to filesystem and and a good replication.

  .. code-block:: bash

    hadoop::dn
  
A-2) Github References (Berksfile)
''''''''''''''''''''''''''''''''''

  .. code-block:: bash

    cookbook 'hadoop', github: 'hopshadoop/apache-hadoop-chef'

B) Experiment: Word-count on Flink 
::::::::::::::::::::::::::::::::::
The following code snippet runs flink wordcount on the generated text in the previous section. As you you can see parallesation parameter is the number of hadoop datanode (flink taskmanager) and it depends on size of your cluster. Namenode address will be also binded at runtime. 

  .. code-block:: bash

    /srv/hadoop/bin/hdfs dfs -rm -r -f /counts
    cd /usr/local/flink
    ./bin/flink run -p #{node.hadoop.dn.public_ips.size} -j ./examples/flink-java-examples-0.9.1-WordCount.jar hdfs:///words/ hdfs://#{node.hadoop.nn.public_ips[0]}:29211/counts

B-1) Orchestration (Karamelfile)
''''''''''''''''''''''''''''''''
If we call our textgenertor recipe "generator::experiment", our wordcount is dependent on that and taskmanager of flink. Karamel will make sure that dependency globally in the cluster.

  .. code-block:: bash

    generator::experiment
    flink::taskmanager
  
B-2) Github References (Berksfile)
''''''''''''''''''''''''''''''''''
We use our hadoop and flink cookbooks because they are already Karamelized. 

  .. code-block:: bash

    cookbook 'hadoop', github: 'hopshadoop/apache-hadoop-chef'
    cookbook 'flink', github: 'hopshadoop/flink-chef'