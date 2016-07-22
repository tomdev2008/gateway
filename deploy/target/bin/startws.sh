#!/bin/bash
################################################################
#@Authro Lijian
#@Date 2007-05-25
################################################################

BASE_BIN_DIR=`dirname $0`
. $BASE_BIN_DIR/env.sh
HOST_NAME=`hostname`

JBOSS_CTL=$BASE_BIN_DIR/jbossctl.sh
TOMCAT_CTL=$BASE_BIN_DIR/tomcatctl.sh

## start jboss 
start_jboss(){
    echo -e "[INFO] $HOST_NAME: starting jboss ...\c"
    chmod +x $BASE_BIN_DIR/jbossctl.sh
    $JBOSS_CTL > $JBOSS_STDOUT_LOG 2>&1 &
    COUNT=0
    printProcess 5
    while [ $COUNT -lt 1 ]; do    
        COUNT=`curl -s $CHECK_SERVER_STARTUP_URL |grep -c "$STARTUP_SUCCESS_MSG"`
        printProcess 3        
    done
    success "Oook!"
}

## start tomcat
start_tomcat(){
    echo -e "[INFO] $HOST_NAME: starting tomcat ...\c"
    chmod +x $BASE_BIN_DIR/tomcatctl.sh
    $TOMCAT_CTL &
    COUNT=0
    printProcess 5
    while [ $COUNT -lt 1 ]; do    
        COUNT=`curl -s $CHECK_SERVER_STARTUP_URL |grep -c "$STARTUP_SUCCESS_MSG"`
        printProcess 3        
    done
    success "Oook!"
}

if [ $SERVER_TYPE = "jboss" ]; then
    start_jboss
fi

if [ $SERVER_TYPE = "tomcat" ]; then
    start_tomcat
fi

success "[INFO] $HOSTNAME: startup web server done!"