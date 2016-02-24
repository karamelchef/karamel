echo $$ > %pid_file%; echo '#!/bin/bash
if [ %osfamily% == "redhat" ] ; then
rm chefdk-0.8.0-1.el6.x86_64.rpm -f && wget https://opscode-omnibus-packages.s3.amazonaws.com/el/6/x86_64/chefdk-0.8.0-1.el6.x86_64.rpm ; %sudo_command% yum install -y chefdk-0.8.0-1.el6.x86_64.rpm ; echo '%task_id%' >> %succeedtasks_filepath%

elif [ %osfamily% == "ubuntu" ] ; then
rm chefdk_0.6.2-1_amd64.deb -f && wget https://opscode-omnibus-packages.s3.amazonaws.com/ubuntu/12.04/x86_64/chefdk_0.6.2-1_amd64.deb ; %sudo_command% dpkg -i chefdk_0.6.2-1_amd64.deb && echo '%task_id%' >> %succeedtasks_filepath%
echo \"Found ubuntu\"
else 
 echo "Unrecognized version of linux. Not ubuntu or redhat family."
 exit 1
fi
echo '%task_id%' >> ~/%succeedtasks_filepath%
' > berks-install.sh ; chmod +x berks-install.sh ; ./berks-install.sh
