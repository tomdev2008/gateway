set JAVA_MEM_OPT= -Xms1024m -Xmx1024m -XX:MaxPermSize=128m -Djava.net.preferIPv4Stack=true
set JAVA_HOME=$yoho.env.javahome
set WEB_APP_HOME=/home/master/yohobuy-order/deploy




rem set JAVA_DEBUG_OPT= -server -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8082,server=y,suspend=n
rem SET JMX_FILE_ACL="%WEB_APP_HOME%\conf\jmx\*.properties"
rem CACLS %JMX_FILE_ACL%  /t /c /p %USERNAME%:f
rem SET TIGER_JMX_OPT= -Dcom.sun.management.config.file=%WEB_APP_HOME%\conf\jmx\jmx_monitor_management.properties
rem SET JAVA_OPTS=%JAVA_OPTS% %TIGER_JMX_OPT%
