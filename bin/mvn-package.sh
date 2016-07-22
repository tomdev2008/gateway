#!/bin/bash
################################################################
#@Authro Lijian
#@Date 2011-06-08
################################################################

cd ..

/usr/local/maven/bin/mvn clean

/usr/local/maven/bin/mvn eclipse:clean

/usr/local/maven/bin/mvn package -Dmaven.test.skip=true -Dautoconfig.userProperties=$1 -Dautoconfig.sharedProperties=$2




