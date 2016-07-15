echo $$ > %pid_file%; echo '#!/bin/bash
%sudo_command% cat > /usr/local/tablespoon-agent/topics/%file_name%.json << EOF
%file_content%
EOF
echo '%task_id%' >> %succeedtasks_filepath%
' > %file_name%.sh ; chmod +x %file_name%.sh ; %sudo_command% ./%file_name%.sh