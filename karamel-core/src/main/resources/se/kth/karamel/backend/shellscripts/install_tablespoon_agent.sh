echo $$ > %pid_file%; echo '#!/bin/bash
%sudo_command% wget "https://www.dropbox.com/s/byrn8h1bjnrg18g/tablespoon-agent.tar.gz"
%sudo_command% tar zxvf tablespoon-agent.tar.gz -C /usr/local
%sudo_command% wget -O /etc/init.d/tablespoon-agent "https://www.dropbox.com/s/xunhnu04lvmaxab/tablespoon-agent"
%sudo_command% chmod +x /etc/init.d/tablespoon-agent
echo '%task_id%' >> %succeedtasks_filepath%
' > agent-install.sh ; chmod +x agent-install.sh ; ./agent-install.sh
