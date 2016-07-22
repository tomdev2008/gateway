@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set ENV_PATH=.\
if "%OS%" == "Windows_NT" set ENV_PATH=%~dp0%
call %ENV_PATH%\env.bat

set JAVA_OPTS=%JAVA_MEM_OPT% -Dcatalina.base=%CATALINA_BASE% %JAVA_DEBUG_OPT% %TIGER_JMX_OPT%
set PATH=%JAVA_HOME%\bin;%PATH%

if not exist %CATALINA_BASE% (
        md "%CATALINA_BASE%"
        md "%CATALINA_BASE%\conf"
        md "%CATALINA_BASE%\webapps"
)

XCOPY  "%CATALINA_HOME%\conf\."  "%CATALINA_BASE%\conf\."  /EIKQ
COPY  "%WEB_APP_HOME%\conf\tomcat\server.xml" "%CATALINA_BASE%\conf\server.xml" /Y
COPY  "%WEB_APP_HOME%\*.war"  "%CATALINA_BASE%\webapps\."  /Y

call %CATALINA_HOME%\bin\catalina.bat start