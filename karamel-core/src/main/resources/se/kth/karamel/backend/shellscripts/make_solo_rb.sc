mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash
set -eo pipefail
%sudo_command% touch solo.rb
%sudo_command% chmod 777 solo.rb

if [ "%gem_server_port%" != ""] ; then
  netstat -ltpn | grep %gem_server_port%
  if [ $? -ne 0 ] ; then
    %sudo_command% %start_gems_server%
  fi
fi

cat > solo.rb <<-'END_OF_FILE'
file_cache_path "/tmp/chef-solo"
cookbook_path [%cookbooks_path%]
%http_proxy%
%https_proxy%
%gems_server_url%
END_OF_FILE' > make_solo_rb.sh ; chmod +x make_solo_rb.sh ; ./make_solo_rb.sh