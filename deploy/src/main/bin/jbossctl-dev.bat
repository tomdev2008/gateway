@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set ENV_PATH=.\
if "%OS%" == "Windows_NT" set ENV_PATH=%~dp0%
call %ENV_PATH%\env.bat

set JAVA_OPTS=%JAVA_MEM_OPT% %JBOSS_SERVER_BASE_DIR% %JAVA_DEBUG_OPT% %TIGER_JMX_OPT%

if not exist %JBOSS_BASE_DIR% (		
	XCOPY  "%JBOSS_HOME%\standalone\." "%JBOSS_BASE_DIR%\."  /EIKQ
)

rem COPY  "%WEB_APP_HOME%\conf\jboss\deploy\jboss-web.deployer\server.xml" "%JBOSS_SERVER_HOME%\deploy\jboss-web.deployer\server.xml" /Y
rem COPY  "%WEB_APP_HOME%\conf\jboss\jboss-service.xml" "%JBOSS_SERVER_HOME%\conf\jboss-service.xml" /Y
COPY  "%WEB_APP_HOME%\conf\jboss\standalone.xml" "%JBOSS_BASE_DIR%\configuration\standalone.xml" /Y
XCOPY  "%WEB_APP_HOME%\web.ear\."  "%JBOSS_BASE_DIR%\deployments\web.ear\."  /EIKQ
echo "" > %JBOSS_BASE_DIR%\deployments\web.ear.dodeploy

REM cls screen 
REM CLS

call %JBOSS_HOME%\bin\standalone.bat
