#!/bin/bash
set -e
if [ ! -d ../../karamel-chef ] ; then
  echo ""
  echo "You have to checkout git@github.com:logicalclocks/karamel-chef.git in the parent folder for karamel to generate a distribution of karamel"
  echo "cd ../../"
  echo "git clone git@github.com:logicalclocks/karamel-chef.git"
  echo ""
  exit 1
fi

karamelize() {

    cd appassembler/bin
    head -n -9 karamel > karamel.tmp
    echo '
exec "$JAVACMD" $JAVA_OPTS -Xms128m -Xmx4g \
  -classpath "$CLASSPATH" \
  -Dapp.name="karamel" \
  -Dapp.pid="$$" \
  -Dapp.repo="$REPO" \
  -Dapp.home="$BASEDIR" \
  -Dhttp.proxyHost="$http_proxy"\
  -Dhttp.proxyPort="$http_proxy_port"\
  -Dhttps.proxyHost="$https_proxy"\
  -Dhttps.proxyPort="$https_proxy_port"\
  -Dbasedir="$BASEDIR" \
  se.kth.karamel.webservice.KaramelServiceApplication \
  -server conf/dropwizard.yml "$@"
' >> karamel.tmp
    mv -f karamel.tmp karamel
    chmod +x karamel
    cd ../..
}


# This reads the pom.xml file in the current directory, and extracts the first version element in the xml version element.
version=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)

echo "version is: $version"

dist=karamel-$version

cd ..
mvn clean package -DskipTests
cd ../karamel-chef
git pull
cd ../karamel
cd karamel-ui/target
karamelize
#create linux archive
cp -r appassembler/* $dist/
cp ../README.linux $dist/README.txt
mkdir $dist/examples
cp ../../../karamel-chef/cluster-defns/* $dist/examples/
tar zcf ${dist}.tgz $dist
mv $dist ${dist}-linux

#create jar archive
mkdir ${dist}-jar
cp karamel-ui-${version}-shaded.jar ${dist}-jar/karamel-ui-${version}.jar

cp -r appassembler/conf/* ${dist}-jar/
cp ../README.linux ${dist}-jar/README.txt
mkdir ${dist}-jar/examples
cp ../../../karamel-chef/cluster-defns/* ${dist}-jar/examples/
zip -r ${dist}-jar.zip $dist-jar


scp ${dist}.tgz glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/
scp ${dist}-jar.zip glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/

echo "Now building windows distribution"
cd ../..
mvn -Dwin clean package -DskipTests
cd karamel-ui/target

mv karamel.exe $dist/karamel.exe
#create windows archive
cp ../README.windows $dist/README.txt
mkdir $dist/examples
cp ../../../karamel-chef/cluster-defns/* $dist/examples/
zip -r ${dist}.zip $dist

mv ${dist} ${dist}-windows

scp ${dist}.zip glassfish@snurran.sics.se:/var/www/karamel.io/sites/default/files/downloads/

echo "finished releasing karamel"
