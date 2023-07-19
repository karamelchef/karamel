set -eo pipefail; mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash

RES=0
if [ %osfamily% == "redhat" ] ; then

  yum list installed cinc-workstation
  if [ $? -ne 0 ] ; then
    chefdkfile='cinc-workstation-%chefdk_version%-1.el8.x86_64.rpm'

    rm -f "$chefdkfile"
    wget "https://repo.hops.works/master/$chefdkfile"

    %sudo_command% yum install -y "$chefdkfile"
    RES=$?
    if [ $RES -ne 0 ] ; then
      sleep 10
      %sudo_command% yum install -y "$chefdkfile"
    fi
  fi

elif [ %osfamily% == "ubuntu" ] ; then

  dpkg -s chefdk
  if [ $? -ne 0 ] ; then
    chefdkfile='cinc-workstation_%chefdk_version%-1_amd64.deb'
    rm -f "$chefdkfile"
    wget "https://repo.hops.works/master/$chefdkfile"

    %sudo_command% dpkg -i "$chefdkfile"
    RES=$?
    if [ $RES -ne 0 ] ; then
      sleep 10
      %sudo_command% dpkg -i "$chefdkfile"
    fi
  fi
else 
 echo "Unrecognized version of linux. Not ubuntu or redhat family."
 exit 1
fi
exit $RES
' > install-chefdk.sh ; chmod +x install-chefdk.sh ; ./install-chefdk.sh
