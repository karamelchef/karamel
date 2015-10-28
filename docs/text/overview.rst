Overview
--------
Karamel is a set of management tools that simplifies reproducing distributed experiments on multi-cloud environment. Infrastructure, software, parameters, data and experimenter are different elements involved in Karamelized experiments. In Karamel, infrastructure and software are delivered as code while parameters are modifiable. By using Github as artifact-server, all experiment artifacts are globally available - any person around the globe can replay/reproduce any experiment.
Karamel leverages virtual-machines to provision infrastructures on different clouds. We have cloud-connectors for Amazon EC2, Google Compute Engine and in house premises (bare-metal) that are fully working now and Openstack Nova is under construction. 
Sofware definition in Karamel is made on top of `Chef <Chef>`_ - Chef is a a dependency aware configuration and installation tool-set - while cluster-wide orchestration mechanism belongs to Karamel. 
In Karamel two level of development exist, to design and to run. Designing is a level of development to make a new system or experiments in a way that is runnable via Karamel, in that regard designer should have knowledge about Chef to some extent. Users are also somewhat developers but it is enough if you are familiar with our DSL for cluster definition.

.. _Chef: https://www.chef.io/
.. _hopsHop: https://github.com/hopshadoop