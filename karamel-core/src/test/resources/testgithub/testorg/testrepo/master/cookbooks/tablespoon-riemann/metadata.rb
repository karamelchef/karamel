name             'tablespoon-riemann'
maintainer       "tablespoon-riemann"
maintainer_email "k.hakimzadeh@gmail.com"
license          "Apache v2.0"
description      'Installs/Configures/Runs riemann for Tablespoon'
version          "0.1"

recipe            "tablespoon-riemann::install", "Setup for riemann"
recipe            "tablespoon-riemann::dash",    "Dashboard setup"
recipe            "tablespoon-riemann::server",  "server setup" 


depends "kagent"
depends "runit"
depends "java"
depends "ark"


%w{ ubuntu debian rhel centos }.each do |os|
  supports os
end



attribute "tablespoon-riemann/group",
:description => "group parameter value",
:type => "string"

attribute "tablespoon-riemann/user",
:description => "user parameter value",
:type => "string"

attribute "tablespoon-riemann/server/port",
:description => "riemann server port number",
:type => "string"
