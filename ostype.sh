#!/bin/bash
UNAME=$(uname | tr \"[:upper:]\" \"[:lower:]\")
# If Linux, try to determine specific distribution
echo \"name: $UNAME\"
if [ \"$UNAME\" == \"linux\" ]; then
    # If available, use LSB to identify distribution
    if [ -f /etc/lsb-release -o -d /etc/lsb-release.d ]; then
        export DISTRO=$(lsb_release -i | cut -d: -f2 | sed s/^t//)
    # Otherwise, use release info file
    else
        export DISTRO=$(ls -d /etc/[A-Za-z]*[_-][rv]e[lr]* | grep -v \"lsb\" | cut -d/ -f3 | cut -d- -f1 | cut -d_ -f1)
    fi
fi
echo "$DISTRO" > ostype.txt

