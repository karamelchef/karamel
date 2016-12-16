mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash
set -eo pipefail
echo $(date '+%H:%M:%S'): '%json_file_name%' >> order
cat > %json_file_name%.json <<-'END_OF_FILE'
%chef_json%
END_OF_FILE
%sudo_command% chef-solo -c solo.rb -j %json_file_name%.json 2>&1 | tee %log_file_name%.log 
echo '%task_id%' >> %succeedtasks_filepath%
' > %json_file_name%.sh ; chmod +x %json_file_name%.sh ; ./%json_file_name%.sh
