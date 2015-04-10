# Karamel - the orchestrator for Chef

Website: http://www.karamel.io

Mailing list: <a href="https://groups.google.com/forum/#!forum/karamel-chef">Google Groups</a>

Karamel is an orchestration engine for Chef Solo that enables the deployment of large distributed systems on both virtualized platforms (AWS, Vagrant) and bare-metal hosts. 

A distributed system is defined in YAML as a set of node groups that each implement a number of Chef recipes, where the Chef cookbooks are deployed on github.  

Karamel introduces a Karamelfile to orchestrate the execution of Chef recipes. Karmelfiles are written in YAML and define dependencies between recipes, that is, the order in which Chef recipes should be run.

At the cluster level, the set of Karamelfiles defines a directed acyclic graph (DAG) of recipe dependencies. 

Karamel system definitions are very compact. We leverage Berkshelf to transparently download and install transitive cookbook dependencies, so large systems can be defined in a few lines of code. Finally, the Karamel runtime builds and manages the execution of the DAG of Chef recipes, by first launching the virtual machines or configuring the bare-metal boxes and then executing recipes with Chef Solo. 

Karamel provides a web-ui, built on AngularJS.


