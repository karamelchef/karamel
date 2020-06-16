mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash
export $http_proxy=%http_proxy%
export $https_proxy=%https_proxy%

if [ %osfamily% == "redhat" ] ; then
%sudo_command% perl -pi -e "s/Defaults\s*requiretty/#Defaults   requiretty/g" /etc/sudoers
%sudo_command% systemctl stop firewalld
%sudo_command% systemctl disable firewalld
%sudo_command% yum check-update -y
%sudo_command% yum install curl -y
%sudo_command% yum install git -y
%sudo_command% yum install make -y
%sudo_command% yum install wget -y

elif [ %osfamily% == "ubuntu" ] ; then
%sudo_command% apt-get update -y
%sudo_command% apt-get update -y
%sudo_command% apt-get install -f -y --force-yes git 
%sudo_command% apt-get install -f -y --force-yes curl
%sudo_command% apt-get install -f -y --force-yes make


else
 echo "Unrecognized version of linux. Not ubuntu or redhat family."
 exit 1
fi

git config --global user.name %github_username% 
git config --global http.sslVerify false
git config --global http.postBuffer 524288000
if [ $http_proxy != "" ] ; then
   git config --global http.proxy $http_proxy
elif [ $https_proxy != "" ] ; then
   git config --global http.proxy $https_proxy   
fi


echo '%task_id%' >> %succeedtasks_filepath%
' > aptget.sh ; chmod +x aptget.sh ; ./aptget.sh