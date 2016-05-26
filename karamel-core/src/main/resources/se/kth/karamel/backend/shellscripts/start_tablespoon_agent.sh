echo $$ > %pid_file%; echo '#!/bin/bash
%sudo_command% service tablespoon-agent start
echo '%task_id%' >> %succeedtasks_filepath%
' > agent-start.sh ; chmod +x agent-start.sh ; ./agent-start.sh