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
%sudo_command% yum check-update -y
%sudo_command% yum install curl -y
%sudo_command% yum install git -y
%sudo_command% yum install make -y
%sudo_command% yum install wget -y
git config --global user.name %github_username% 
git config --global http.sslVerify false
git config --global http.postBuffer 524288000
elif [ $OS_TYPE -eq 2 ] ; then
%sudo_command% apt-get update -y
%sudo_command% apt-get update -y
%sudo_command% apt-get install -f -y --force-yes git 
%sudo_command% apt-get install -f -y --force-yes curl
%sudo_command% apt-get install -f -y --force-yes make
git config --global user.name %github_username%
git config --global http.sslVerify false
git config --global http.postBuffer 524288000
echo \"Found ubuntu\"
else # ubuntu
 echo \"Unrecognized version of linux. Not ubuntu or redhat family.\"
fi
' > build.sh ; chmod +x build.sh ; ./build.sh