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


Operating Systems
======================

Ubuntu
--------------
Karamel supports Ubuntu natively. It should just work for Ubuntu.

Centos/Redhat  (Requiretty)
------------------------------

Centos has some extra security features that need to be addressed when using Karamel.
First, you need to disable the requiretty option in sudoers file:
The requiretty if set in sudo config file sudoers, sudo will only run when the user is logged in to a real tty. When this flag is set, sudo can only be run from a login session and not via other means such as cron, shell/perl/python or cgi-bin scripts. This flag is set on many distores by default. Edit /etc/sudoers, file, enter:
# visudo

Find line the following line and comment it out:

```Defaults    requiretty```

change to:

``` #Defaults    requiretty```


You may also need to disable firewalls (or open the correct ports):

```systemctl stop firewalld```
```systemctl disable firewalld```
