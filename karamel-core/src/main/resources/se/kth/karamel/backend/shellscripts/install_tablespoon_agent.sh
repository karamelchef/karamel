echo $$ > %pid_file%; echo '#!/bin/bash
mkdir /usr/local/tablespoon-agent
wget -O /usr/local/tablespoon-agent/tablespoon-agent.jar "https://www.dropbox.com/s/klmxlg10pme3f1q/tablespoon-agent.jar"
wget -O /etc/init.d/tablespoon-agent "https://www.dropbox.com/s/xunhnu04lvmaxab/tablespoon-agent"
chmod +x /etc/init.d/tablespoon-agent' > agent-install.sh ; chmod +x agent-install.sh ; ./agent-install.sh