echo $$ > %pid_file%; echo '#!/bin/bash
%sudo_command% service tablespoon-agent stop
echo '%task_id%' >> %succeedtasks_filepath%
' > agent-stop.sh ; chmod +x agent-stop.sh ; ./agent-stop.sh