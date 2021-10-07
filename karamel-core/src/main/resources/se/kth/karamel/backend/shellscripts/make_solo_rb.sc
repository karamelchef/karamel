mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash
# set -eo pipefail
%sudo_command% touch solo.rb
if [ $? -ne 0 ] ; then
  echo "Problem touching solo.rb file"
  exit 1
fi
%sudo_command% chmod 777 solo.rb

echo "Making solo.rb" > make_solo.log

if [ "%gems_server_port%" != "" ] ; then
  echo "ported" >> make_solo.log
  %sudo_command% netstat -ltpn | grep %gems_server_port%
  if [ $? -ne 0 ] ; then
    # Only start the gem server on the head karamel node
    if [ -d %gems_src_dir% ] ; then
      %sudo_command% cp -f %gems_src_dir%/chefdk_gems.tgz /root
      %sudo_command% tar zxf /root/chefdk_gems.tgz -C /root
      echo "Starting gem server" >> make_solo.log
      echo "%sudo_command% %start_gems_server%" >> make_solo.log
      %sudo_command% %start_gems_server%
      sleep 2
      %sudo_command% netstat -ltpn | grep %gems_server_port%
      if [ $? -ne 0 ] ; then
        echo "Retrying gem server" >> make_solo.log
        %sudo_command% %start_gems_server%
        sleep 2
        %sudo_command% netstat -ltpn | grep %gems_server_port%
        if [ $? -ne 0 ] ; then
          echo "Problem starting local gem server with command: " >> make_solo.log
          echo "%start_gems_server%" >> make_solo.log
          exit 12
        fi
      fi
    fi
  else
    echo "gem server already running" >> make_solo.log
  fi
else
  echo "Not starting gem server" >> make_solo.log
fi

cat > solo.rb <<-'END_OF_FILE'
file_cache_path "/tmp/chef-solo"
cookbook_path [%cookbooks_path%]
%http_proxy%
%https_proxy%
%gems_server_url%
END_OF_FILE' > make_solo_rb.sh ; chmod +x make_solo_rb.sh ; ./make_solo_rb.sh