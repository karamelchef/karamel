#link:Visit our {website,www.hops.io}
name             'hopshub'
maintainer       "Jim Dowling"
maintainer_email "jdowling@kth.se"
license          "Apache v2.0"
description      "Installs/Configures the HopsHub Dashboard"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"

%w{ ubuntu debian centos rhel }.each do |os|
  supports os
end

depends 'glassfish'
depends 'ndb'
depends 'kagent'

recipe  "hopshub::install", "Installs HopsHub/Glassfish"
#link:Click {here,https://%host%:8181/hop-dashboard} to launch hopshub in your browser
#link:Visit Karamel {here,www.karamel.io}
recipe  "hopshub::default", "Installs HopsHub war file, starts glassfish+application."

attribute "hopshub/smtp/server",
:display_name => "Smtp server address for sending emails",
:description => "Smtp server address for sending emails",
:type => 'string',
:default => "smtp.gmail.com"

attribute "hopshub/smtp/port",
:display_name => "Smtp server port for sending emails",
:description => "Smtp server port for sending emails",
:type => 'string',
:default => "465"

attribute "hopshub/smtp/secure",
:display_name => "Use SSL to Smtp server",
:description => "Use SSL to Smtp server",
:type => 'string',
:default => "true"

attribute "hopshub/smtp/username",
:display_name => "Email account username",
:description =>  "Email account username",
:type => 'string',
:default => "sodugling@gmail.com"

attribute "hopshub/smtp/password",
:display_name => "Email account password",
:description =>  "Email account password",
:type => 'string',
:default => "admin"

attribute "kagent/enabled",
:display_name => "Install kagent",
:description =>  "Install kagent",
:type => 'string',
:default => "false"