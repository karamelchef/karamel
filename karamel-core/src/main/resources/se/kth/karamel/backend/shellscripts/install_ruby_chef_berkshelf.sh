#!/bin/bash

BINARIES=https://opscode-omnibus-packages.s3.amazonaws.com/ubuntu/12.04/x86_64/chefdk_0.4.0-1_amd64.deb
CHEFDK=`echo $BINARIES | sed 's/.*\///'`

if [ -f chefdk_0.1.0-1_amd64-ubuntu12.deb ] ; then
  sudo dpkg -i $CHEFDK
  if [ $? -ne 0 ] ; then
      rm $CHEFDK
      wget $BINARIES
      sudo dpkg -i $CHEFDK
  fi
else
  wget $BINARIES
  sudo dpkg -i $CHEFDK
fi
if [ $? -ne 0 ] ; then
  exit 1
fi

exit 0
