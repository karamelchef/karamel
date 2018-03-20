set -eo pipefail; mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash

#set -eo pipefail
RES=0
if [ %osfamily% == "redhat" ] ; then
  chefdkfile='chefdk-%chefdk_version%-1.el7.x86_64.rpm'

  rm -f "$chefdkfile"
  wget "http://snurran.sics.se/hops/$chefdkfile"

  %sudo_command% yum install -y "$chefdkfile"
  RES=$?
  if [ $RES -ne 0 ] ; then
    sleep 10
    %sudo_command% yum install -y "$chefdkfile"
  fi

elif [ %osfamily% == "ubuntu" ] ; then
  chefdkfile='chefdk_%chefdk_version%-1_amd64.deb'

  rm -f "$chefdkfile"
  wget "http://snurran.sics.se/hops/$chefdkfile"

  %sudo_command% dpkg -i "$chefdkfile"
  RES=$?
  if [ $RES -ne 0 ] ; then
    sleep 10
    %sudo_command% dpkg -i "$chefdkfile"
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
