echo $$ > %pid_file%; echo '#!/bin/bash
%sudo_command% service tablespoon-agent start
echo '%task_id%' >> %succeedtasks_filepath%
' > tablespoon-agent-start.sh ; chmod +x tablespoon-agent-start.sh ; ./tablespoon-agent-start.sh