Overview
--------

Karamel is a set of management tools for provisioning, orchestration, monitoring and debugging of large distributed systems and experiments. We support public clouds (Amazon EC2 and Google Compute Engine), general purpose cloud manager (Openstack Nova) and in-house premises (bare-metal).

Karamel provisions network and virtual machines via cloud-providers' API, physical layer, while installation and running experiments happens through direct ssh connection with virtual machines, sofware layer. Therefore for adding a support for new cloud-provider it is enough to be handled in the physical layer because software layer is general purpose.

Karamel uses Chef_ - Chef is an automation platform for web scale IT, in Chef softwares are delivered as code having version, author and dependencies - for executing software scripts by handling dependencies on a single machine while it brings cluster-wide dependencies on top of Chef. Software scripts that Karamel run via chef are installation and configuration as well as computation and experimentation. 

Software packages in Karamel are cookbooks in Chef, each cookbook is deployed separately into Github. Each cookbook contains set of recipes and recipe is a unit of software for installing a service or running an experiment. Some of our predefined cookbooks can be found in HopsHop_.

Karamel introduces Karamelfile to orchestrate the execution of Chef recipes. Karmelfiles are written in YAML and define dependencies between recipes, that is, the order in which Chef recipes should be run. At the cluster level, the set of Karamelfiles defines a directed acyclic graph (DAG) of recipe dependencies.

We leverage Berkshelf to transparently download and install transitive cookbook dependencies, so large systems can be defined in a few lines of code. Finally, the Karamel runtime builds and manages the execution of the DAG of Chef recipes by executing them with Chef Solo

.. _Chef: https://www.chef.io/
.. _hopsHop: https://github.com/hopshadoop