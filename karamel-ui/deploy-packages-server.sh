#!/bin/bash

if [ $# -ne 1 ] ; then
echo "Usage: <prog> release-number"
exit 1
fi

dist=karamel-$1

mvn clean package
cd target

#create linux archive
mv appassembler $dist
tar zcf ${dist}.tgz ${dist}/
mv $dist ${dist}-linux

#create windows archive
mv windows $dist
zip -r ${dist}.zip $dist

#create jar archive
mkdir ${dist}-jar
mv karamel-ui-$1-shaded.jar ${dist}-jar
cp ${dist}/conf/* ${dist}-jar/ 
zip -r ${dist}-jar.zip $dist-jar

scp ${dist}.tgz glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/
scp ${dist}.zip glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/
scp ${dist}-jar.zip glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/
cd ..


