mkdir -p %install_dir_path% ; cd %install_dir_path%; echo $$ > %pid_file%; echo '#!/bin/bash
set -eo pipefail
UNAME=$(uname | tr \"[:upper:]\" \"[:lower:]\")
# If Linux, try to determine specific distribution
echo \"name: $UNAME\"
if [ \"$UNAME\" == \"linux\" ]; then
    # If available, use LSB to identify distribution
    if [ -f /etc/lsb-release -o -d /etc/lsb-release.d ]; then
        export DISTRO=$(lsb_release -i | cut -d: -f2 | sed s/'^\t'//)
    # Otherwise, use release info file
    else
        export DISTRO=$(ls -d /etc/[A-Za-z]*[_-][rv]e[lr]* | grep -v \"lsb\" | cut -d'/' -f3 | cut -d'-' -f1 | cut -d'_' -f1)
        if [ "$DISTRO" == "Ubuntu" ] || [ "$DISTRO" == "centos" ] ; then
            echo "Found os: $DISTRO"
        else
          # =~ is a case-insentive substring match
          if [[ $DISTRO =~ "Ubuntu" ]]; then
                DISTRO="Ubuntu"
          elif [[ $DISTRO =~ "centos" ]] || [[ "$DISTRO" =~ "os" ]] ; then
                DISTRO="centos"
          else
                echo "Could not recognize Linux distro: $DISTRO"
                exit_error
          fi
        fi
    fi
fi
echo "$DISTRO" > %ostype_filename%
echo '%task_id%' > %succeedtasks_filepath%
' > %ostype_filename%.sh ; chmod +x %ostype_filename%.sh ; ./%ostype_filename%.sh
