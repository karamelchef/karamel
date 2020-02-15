set -eo pipefail; mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash

RES=0
if [ %osfamily% == "redhat" ] ; then

  yum list installed chefdk
  if [ $? -ne 0 ] ; then
    chefdkfile='chefdk-%chefdk_version%-1.el7.x86_64.rpm'

    rm -f "$chefdkfile"
    wget "https://packages.chef.io/files/stable/chefdk/3.7.23/el/7/chefdk-3.7.23-1.el7.x86_64.rpm"

    %sudo_command% yum install -y "$chefdkfile"
    RES=$?
    if [ $RES -ne 0 ] ; then
      sleep 10
      %sudo_command% yum install -y "$chefdkfile"
    fi
  fi

elif [ %osfamily% == "ubuntu" ] ; then

  dpkg -s
  if [ $? -ne 0 ] ; then
 
    chefdkfile='chefdk_%chefdk_version%-1_amd64.deb'

    rm -f "$chefdkfile"
    wget "https://packages.chef.io/files/stable/chefdk/3.7.23/ubuntu/18.04/chefdk_3.7.23-1_amd64.deb"

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
if [ $RES -eq 0 ] ; then
  echo '%task_id%' >> %succeedtasks_filepath%
fi
exit $RES
' > install-chefdk.sh ; chmod +x install-chefdk.sh ; ./install-chefdk.sh
