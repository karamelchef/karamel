set -eo pipefail; mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash
set -eo pipefail

curl --connect-timeout 10 -m 120 https://omnitruck.chef.io/install.sh | sudo bash -s -- -c stable -P chefdk && echo '%task_id%' >> %succeedtasks_filepath%

exit

set -eo pipefail
if [ %osfamily% == "redhat" ] ; then
  chefdkfile='chefdk-%chefdk_version%-1.el7.x86_64.rpm'

  if [ -f $chefdkfile ] ; then
    rm -f "$chefdkfile"
  else
    wget "http://snurran.sics.se/hops/$chefdkfile"
  fi

  %sudo_command% yum install -y "$chefdkfile" && echo '%task_id%' >> %succeedtasks_filepath%

elif [ %osfamily% == "ubuntu" ] ; then
  chefdkfile='chefdk_%chefdk_version%-1_amd64.deb'

  if [ -f $chefdkfile ] ; then
    rm -f "$chefdkfile"
  else
    wget "http://snurran.sics.se/hops/$chefdkfile"
  fi

  %sudo_command% dpkg -i "$chefdkfile" && echo '%task_id%' >> %succeedtasks_filepath%
  echo \"Found ubuntu\"
else 
 echo "Unrecognized version of linux. Not ubuntu or redhat family."
 exit 1
fi
echo '%task_id%' >> %succeedtasks_filepath%
' > install-chefdk.sh ; chmod +x install-chefdk.sh ; ./install-chefdk.sh
