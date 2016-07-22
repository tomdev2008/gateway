#!/bin/bash
################################################################
#@Authro Lijian
#@Date 2015-10-22
################################################################

BASE_BIN_DIR=`dirname $0`
. $BASE_BIN_DIR/env.sh

export CATALINA_HOME=$SERVER_HOME
export CATALINA_BASE=$WEB_APP_HOME/.tomcat

################################################################
##check if started before
check_server(){
    java_pid=`ps  --no-heading -C java -f --width 1000 | grep "$$CATALINA_BASE" |awk '{print $2}'`
    if [ ! -z "$java_pid" ]; then
        echo "[INFO] Tomcat server already started: pid=$java_pid"
        exit;
    fi
}

copy_server_home(){
    if [ -d "$CATALINA_BASE" ]; then
        rm -rf $CATALINA_BASE
    fi
    mkdir -p $CATALINA_BASE/conf
    mkdir -p $CATALINA_BASE/webapps
	mkdir -p $CATALINA_BASE/logs
	mkdir -p $CATALINA_BASE/temp
	touch $CATALINA_BASE/logs/catalina.out
}

copy_tomcat_conf(){
    cp -rf $SERVER_HOME/conf/. $CATALINA_BASE/conf/.
    cp -rf $WEB_APP_HOME/conf/tomcat/server.xml $CATALINA_BASE/conf/server.xml
}

copy_war(){
	if [ ! -d $WEB_APP_HOME/$SERVER_NAMESPACE-war ]; then
		mkdir -p $WEB_APP_HOME/$SERVER_NAMESPACE-war
		cd $WEB_APP_HOME/$SERVER_NAMESPACE-war
		mv $WEB_APP_HOME/*.war $WEB_APP_HOME/$SERVER_NAMESPACE-war/$SERVER_NAMESPACE.war

		$JAVA_HOME/bin/jar xf $SERVER_NAMESPACE.war
		rm -rf $WEB_APP_HOME/$SERVER_NAMESPACE-war/$SERVER_NAMESPACE.war
	fi
    cp -r $WEB_APP_HOME/$SERVER_NAMESPACE-war $CATALINA_BASE/webapps/$SERVER_NAMESPACE
}

deploy(){
    check_server
    copy_server_home
    copy_tomcat_conf
    copy_war
    
}
    
###############################################################
deploy
$SERVER_HOME/bin/catalina.sh start > /dev/null