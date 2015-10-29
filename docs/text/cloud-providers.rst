:: _cloud-providers:

Cloud Providers
===============
Karamel allows that each group of machines to co-locate in one cloud while each group can be placed in a different cloud. In the multi-cloud model, geographical location of data-centers matter not cloud providers.

Most of the cloud providers have security-group notion, that is only machines inside each security group are visible to each other. Cross-group and public access must be explicitly granted within each security group. By default Karamel maps each group of machines in the :ref:`cluster definition <cluster-definition>` to a separate security group in cloud. 

AWS (Amazon EC2)
----------------


Google Compute Engine
---------------------

Bare-metal
----------