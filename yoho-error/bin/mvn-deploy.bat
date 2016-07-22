@echo off

cd ..
mvn deploy -DrepositoryId=snapshots -Durl=http://192.168.102.168:18081/nexus/content/repositories/snapshots &pause