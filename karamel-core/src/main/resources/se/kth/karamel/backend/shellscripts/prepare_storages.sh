mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash
symChef=0
for i in %device_mountpoint_tuple% ; do 

IFS=","
set $i

if [ -e $1 ]
  then 
    echo "$1 is existed"

    if grep $1 /etc/mtab > /dev/null 2>&1; 
    then 
      echo "$1 was already mounted"
    else 
      echo "$1 was not mounted, installing ext4 and mounting it"
      %sudo_command% mkfs -F -t ext4 $1
      %sudo_command% mkdir -p -m 777 $2
      %sudo_command% mount $1 $2
      if [ "$symChef" -eq "0" ]; then
        %sudo_command% mkdir -p -m 755 $2/.chef-solo
        %sudo_command% ln -s $2/.chef-solo /tmp/chef-solo
        symChef=1
      fi
    fi

  else 
    echo "$i is not existed, ignoring it"
fi

done
echo '%task_id%' >> %succeedtasks_filepath%
' > prepare_storage.sh ; chmod +x prepare_storage.sh ; ./prepare_storage.sh