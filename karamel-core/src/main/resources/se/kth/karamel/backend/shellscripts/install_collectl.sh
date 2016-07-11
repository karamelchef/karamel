echo $$ > %pid_file%; echo '#!/bin/bash
%sudo_command% wget http://sourceforge.net/projects/collectl/files/collectl/collectl-4.0.4/collectl-4.0.4.src.tar.gz
%sudo_command% tar -xvzf collectl-4.0.4.src.tar.gz
cd collectl-4.0.4
%sudo_command% ./INSTALL 
cd ..
echo '%task_id%' >> %succeedtasks_filepath%
' > collectl-install.sh ; chmod +x collectl-install.sh ; ./collectl-install.sh
