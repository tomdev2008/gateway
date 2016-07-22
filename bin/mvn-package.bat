@echo off

cd ..

mvn package -e -Dmaven.test.skip=true&pause