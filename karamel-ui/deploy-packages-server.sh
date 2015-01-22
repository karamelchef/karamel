#!/bin/bash

set -e

# This reads the pom.xml file in the current directory, and extracts the first version element in the xml version element.
version=`grep -o -a -m 1 -h -r "version>.*</version" pom.xml | head -1 | sed "s/version//g" | sed "s/>//" | sed "s/<\///g"`

dist=karamel-$version

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
mv karamel-ui-${version}-shaded.jar ${dist}-jar
cp ${dist}/conf/* ${dist}-jar/ 
zip -r ${dist}-jar.zip $dist-jar

scp ${dist}.tgz glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/
scp ${dist}.zip glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/
scp ${dist}-jar.zip glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/
cd ..


