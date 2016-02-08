echo $$ > %pid_file%; echo '#!/bin/bash
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
    fi
fi
echo \"distro: $DISTRO\"
OS_TYPE=0
echo $DISTRO | grep -iq centos
if [ $? -eq 0 ] ; then
 OS_TYPE=1
fi
echo $DISTRO | grep -iq fedora
if [ $? -eq 0 ] ; then
 OS_TYPE=1
fi
echo $DISTRO | grep -iq redhat
if [ $? -eq 0 ] ; then
 OS_TYPE=1
fi
echo $DISTRO | grep -iq ubuntu
if [ $? -eq 0 ] ; then
 OS_TYPE=2
fi
echo $DISTRO | grep -iq debian
if [ $? -eq 0 ] ; then
 OS_TYPE=2
fi
if [ $OS_TYPE -eq 1 ] ; then
rm chefdk-0.8.0-1.el6.x86_64.rpm -f && wget https://opscode-omnibus-packages.s3.amazonaws.com/el/6/x86_64/chefdk-0.8.0-1.el6.x86_64.rpm && %sudo_command% yum install -y chefdk-0.8.0-1.el6.x86_64.rpm && echo '%task_id%' >> %succeedtasks_filepath%

elif [ $OS_TYPE -eq 2 ] ; then
rm chefdk_0.6.2-1_amd64.deb -f && wget https://opscode-omnibus-packages.s3.amazonaws.com/ubuntu/12.04/x86_64/chefdk_0.6.2-1_amd64.deb && %sudo_command% dpkg -i chefdk_0.6.2-1_amd64.deb && echo '%task_id%' >> %succeedtasks_filepath%
echo \"Found ubuntu\"
else # ubuntu
 echo \"Unrecognized version of linux. Not ubuntu or redhat family.\"
fi' > build.sh ; chmod +x build.sh ; ./build.sh