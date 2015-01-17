#!/bin/bash

if [ $# -ne 1 ] ; then
echo "Usage: <prog> release-number"
exit 1
fi

linux=karamel-linux-mac-$1.tgz
win=karamel-windows-$1.zip

mvn package
cd target/appassembler
tar zcf ../$linux *
cd ../windows
zip -r ../$win *
cd ../..

scp target/$linux glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/
scp target/$win glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/

