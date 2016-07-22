@echo off

cd ..
echo %~dp0

echo Start to execute command

mvn clean&mvn eclipse:clean&pause