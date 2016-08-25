echo $$ > %pid_file%; echo '#!/bin/bash
%sudo_command% service tablespoon-agent stop
echo '%task_id%' >> %succeedtasks_filepath%
' > tablespoon-agent-stop.sh ; chmod +x tablespoon-agent-stop.sh ; ./tablespoon-agent-stop.sh