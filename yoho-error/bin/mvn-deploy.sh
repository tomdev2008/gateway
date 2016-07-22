#!/bin/bash
################################################################
#@Authro Lijian
#@Date 2011-06-08
################################################################

cd ..

/usr/local/maven/bin/mvn mvn deploy -DrepositoryId=snapshots -Durl=http://192.168.102.168:18081/nexus/content/repositories/snapshots &pause