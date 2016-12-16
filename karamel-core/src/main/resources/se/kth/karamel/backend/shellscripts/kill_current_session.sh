mkdir -p %install_dir_path% ; cd %install_dir_path%; echo '#!/bin/bash
if %sudo_command% ps -p $(<%pid_file%) > /dev/null; then %sudo_command% pkill -9 -s $(<%pid_file%); fi
' > kill.sh ; chmod +x kill.sh ; ./kill.sh
