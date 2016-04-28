echo $$ > %pid_file%; echo '#!/bin/bash
%sudo_command% apt-get install collectl -y
echo '%task_id%' >> %succeedtasks_filepath%
' > collectl-install.sh ; chmod +x collectl-install.sh ; ./collectl-install.sh
