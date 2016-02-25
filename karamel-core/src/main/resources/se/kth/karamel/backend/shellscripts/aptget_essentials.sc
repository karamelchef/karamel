echo $$ > %pid_file%; echo '#!/bin/bash
if [ %osfamily% == "redhat" ] ; then
%sudo_command% perl -pi -e "s/Defaults\s*requiretty/#Defaults   requiretty/g" /etc/sudoers
%sudo_command% systemctl stop firewalld
%sudo_command% systemctl disable firewalld
%sudo_command% yum check-update -y
%sudo_command% yum install curl -y
%sudo_command% yum install git -y
%sudo_command% yum install make -y
%sudo_command% yum install wget -y
git config --global user.name %github_username% 
git config --global http.sslVerify false
git config --global http.postBuffer 524288000
elif [ %osfamily% == "ubuntu" ] ; then
%sudo_command% apt-get update -y
%sudo_command% apt-get update -y
%sudo_command% apt-get install -f -y --force-yes git 
%sudo_command% apt-get install -f -y --force-yes curl
%sudo_command% apt-get install -f -y --force-yes make
git config --global user.name %github_username%
git config --global http.sslVerify false
git config --global http.postBuffer 524288000
else
 echo "Unrecognized version of linux. Not ubuntu or redhat family."
 exit 1
fi
echo '%task_id%' >> ~/%succeedtasks_filepath%
' > aptget.sh ; chmod +x aptget.sh ; ./aptget.sh