mkdir -p %working_dir_path% ; cd %working_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash
set -eo pipefail
%sudo_command% touch solo.rb
%sudo_command% chmod 777 solo.rb
cat > solo.rb <<-'END_OF_FILE'
file_cache_path "/tmp/chef-solo"
cookbook_path [%cookbooks_path%]
END_OF_FILE' > make_solo_rb.sh ; chmod +x make_solo_rb.sh ; ./make_solo_rb.sh