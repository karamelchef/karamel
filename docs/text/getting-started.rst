.. _getting-started:

Getting Started
===============

How to run a cluster?
---------------------
.. include:: how-to-run-a-cluster.rst

How to design an experiment?
----------------------------
A single experiment in Karamel is a Chef recipe, they usually contains "experiment" word in their name. Experiments have some parameters and produce some results. In runtime, Karamel binds values for experiment variables and in the end it downloads the experiment results to Karamel running machine. Like other recipes, experiments can have dependencies to other recipes - that are normally system requirements or pre-conditions - this dependencies are defined inside the Karamelfile. For a step by step instruction for designing your experiment have a look into our :ref:`experiment designer <experiment-designer>`


A Simple Experiment: Wordcount on Apache-Flink
----------------------------------------------
.. include:: simple-experiment.rst

How to write a Karamlelized Cookbook
------------------------------------
.. include:: how-to-write-karamelized-cookbook.rst