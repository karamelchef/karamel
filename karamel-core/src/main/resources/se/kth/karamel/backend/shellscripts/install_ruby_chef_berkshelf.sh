#!/bin/bash

if [ -f chefdk_0.1.0-1_amd64-ubuntu12.deb ] ; then
  sudo dpkg -i chefdk_0.1.0-1_amd64-ubuntu12.deb
  if [ $? -ne 0 ] ; then
      rm chefdk_0.1.0-1_amd64-ubuntu12.deb
      wget http://snurran.sics.se/hops/chefdk_0.1.0-1_amd64-ubuntu12.deb
      sudo dpkg -i chefdk_0.1.0-1_amd64-ubuntu12.deb
  fi
else 
  wget http://snurran.sics.se/hops/chefdk_0.1.0-1_amd64-ubuntu12.deb
  sudo dpkg -i chefdk_0.1.0-1_amd64-ubuntu12.deb
fi

exit 0