set JAVA_MEM_OPT= -Xms1024m -Xmx1024m -XX:MaxPermSize=128m -Djava.net.preferIPv4Stack=true
set JAVA_HOME=${yoho.env.javahome}
set WEB_APP_HOME=${yohobuy.order.env.webapphome}

#if("${yoho.env.servertype}"=="jboss")
set JBOSS_HOME=${yoho.env.serverhome}
set JBOSS_BASE_DIR=${yohobuy.order.env.webapphome}/jboss_server
set JBOSS_SERVER_BASE_DIR= -Djboss.server.base.dir=%JBOSS_BASE_DIR%
#end

#if("${yoho.env.servertype}"=="tomcat")
set CATALINA_HOME=${yoho.env.serverhome}
set CATALINA_BASE=${yohobuy.order.env.webapphome}/tomcat_server
#end


rem set JAVA_DEBUG_OPT= -server -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8082,server=y,suspend=n
rem SET JMX_FILE_ACL="%WEB_APP_HOME%\conf\jmx\*.properties"
rem CACLS %JMX_FILE_ACL%  /t /c /p %USERNAME%:f
rem SET TIGER_JMX_OPT= -Dcom.sun.management.config.file=%WEB_APP_HOME%\conf\jmx\jmx_monitor_management.properties
rem SET JAVA_OPTS=%JAVA_OPTS% %TIGER_JMX_OPT%
