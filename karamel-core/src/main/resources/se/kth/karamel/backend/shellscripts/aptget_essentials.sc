%sudo_command% apt-get update -y
%sudo_command% apt-get update -y
%sudo_command% apt-get install -f -y --force-yes git 
%sudo_command% apt-get install -f -y --force-yes curl 
%sudo_command% apt-get install -f -y --force-yes make 
git config --global user.name %github_username%
git config --global http.sslVerify false
git config --global http.postBuffer 524288000
echo '%task_id%' >> %succeedtasks_filepath%
