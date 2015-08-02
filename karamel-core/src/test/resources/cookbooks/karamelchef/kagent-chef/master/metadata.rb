maintainer       "Jim Dowling"
maintainer_email "jdowling@kth.se"
name             "kagent"
license          "GPL 3.0"
description      "Installs/Configures the Hops agent"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"

%w{ ubuntu debian centos }.each do |os|
  supports os
end

depends 'python'
depends 'openssl'
depends 'openssh'
depends 'sudo'
depends 'hostsfile'

recipe "kagent::default", "Installs and configures the Karamel agent"

attribute "kagent/dashboard/ip_port",
:display_name => "Dashboard Ip:port",
:description => " Ip address and port for Dashboard REST API",
:type => 'string',
:default => "10.0.2.15:8080"

attribute "hop/hostid",
:display_name => "HostId",
:description => " One-time password used when registering the host",
:type => 'string'

attribute "kagent/name",
:display_name => "name",
:description => "Cookbook name",
:type => 'string',
:default => "kagent"
