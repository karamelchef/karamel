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

#link:Visit <a target='_blank' href='http://www.hops.io/'>Hop's Website</a> or <a target='_blank' href='http://www.karamel.io/'>Karamel's Website</a>
#link:Click <a target='_blank' href='https://%host%:8181/hop-dashboard'>here</a> to launch hopshub in your browser
recipe  "hopshub", "Installs HopsHub war file, starts glassfish+application."

#
# Required Attributes
#
attribute "hopshub/smtp/server",
          :display_name => "Smtp server address for sending emails",
          :description => "Smtp server address for sending emails",
          :type => 'string',
          :required => "required",
          :default => "smtp.gmail.com"

attribute "hopshub/smtp/port",
          :display_name => "Smtp server port for sending emails",
          :description => "Smtp server port for sending emails",
          :type => 'string',
          :required => "required",
          :default => "465"

attribute "hopshub/smtp/secure",
          :display_name => "Use SSL to Smtp server",
          :description => "Use SSL to Smtp server",
          :type => 'string',
          :required => "required",
          :default => "true"

attribute "hopshub/smtp/username",
          :display_name => "Email account username",
          :description =>  "Email account username",
          :type => 'string',
          :required => "required",
          :default => "sodugling@gmail.com"

attribute "hopshub/smtp/password",
          :display_name => "Email account password",
          :description =>  "Email account password",
          :type => 'string',
          :required => "required",
          :default => "admin"

#
# Optional Attributes
#

attribute "hopshub/admin/user",
          :description => "Username for Hops Admin account",
          :type => 'string',
          :default => "admin"

attribute "hopshub/admin/password",
          :description => "hopshub/admin/password",
          :type => 'string',
          :default => "changeit"
