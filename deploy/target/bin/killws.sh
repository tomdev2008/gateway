#!/bin/bash
################################################################
#@Authro Lijian
#@Date 2007-05-22
################################################################

BASE_BIN_DIR=`dirname $0`
#import home var env
. $BASE_BIN_DIR/env.sh

TIMESTAMP=`date +%Y_%m_%d_%H:%M`
HOST_NAME=`hostname`
LOG_DIR=$WEB_APP_HOME/logs
JBOSS_CHECK_LOG="$LOG_DIR/jboss_stdout.log"
JBOSS_BASE_DIR=$WEB_APP_HOME/.jboss/default
TOMCAT_BASE_DIR=$WEB_APP_HOME/.tomcat
LOCAL_IP=`/sbin/ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:"`
REMOVE_URL="$GW_URL/service_control/unregister?ip=$LOCAL_IP&context=$SERVER_NAMESPACE"

stop_jboss()
{
    JBOSS_JAVA_PID=`get_jboss_pid`
    if [ ! -z "$JBOSS_JAVA_PID" ] ; then
        echo -e "$HOST_NAME: stopping jboss ...\c"
        $JBOSS_HOME/bin/jboss-cli.sh --connect controller=localhost:$NAMING_PORT command=:shutdown > /dev/null 2>&1
        LOOPS=0
        while [ $LOOPS -lt 30 ]; do
            printProcess
            kill -s 0 $JBOSS_JAVA_PID > /dev/null 2>&1
            if [ $? -eq 1 ]; then 
                break
            fi             
            let LOOPS=LOOPS+1
        done      
                        
        JBOSS_JAVA_PID=`get_jboss_pid`
        if [ ! -z "$JBOSS_JAVA_PID" ] ; then
            echo -e "[INFO] kill java process $JBOSS_JAVA_PID .\c"      
            kill -9 $JBOSS_JAVA_PID > /dev/null 2>&1
        fi
        success "Oook!"
    else
         warning "[WARN] $HOST_NAME: jboss not running, who care?"
    fi
}

get_jboss_pid(){    
    STR=`ps -C java -f --width 1000 | grep "$JBOSS_BASE_DIR"|awk '{print $2}'`           
    echo $STR
}

get_tomcat_pid(){    
    STR=`ps -C java -f --width 1000 | grep "$TOMCAT_BASE_DIR"|awk '{print $2}'`           
    echo $STR
}

stop_tomcat(){
    TOMCAT_JAVA_PID=`get_tomcat_pid`
    if [ ! -z "$TOMCAT_JAVA_PID" ] ; then


        success "start to remove service register from zookeeper: $REMOVE_URL"
        curl -m 10 -s -o /dev/null "$REMOVE_URL"
        sleep 10s
        success "success to remove $LOCAL_IP"


        #echo -e "$HOST_NAME: stopping tomcat ...\c"
        #$SERVER_HOME/bin/catalina.sh stop > /dev/null 2>&1
        #LOOPS=0
        #while [ $LOOPS -lt 30 ]; do
            #printProcess
            #kill -s 0 $TOMCAT_JAVA_PID > /dev/null 2>&1
            #if [ $? -eq 1 ]; then 
                #break
            #fi             
            #let LOOPS=LOOPS+1
        #done      
                        
        #TOMCAT_JAVA_PID=`get_tomcat_pid`
        #if [ ! -z "$TOMCAT_JAVA_PID" ] ; then
            echo -e "[INFO] kill java process $TOMCAT_JAVA_PID .\c"      
            kill -9 $TOMCAT_JAVA_PID > /dev/null 2>&1
        #fi
        success "Oook!"
    else
         warning "[WARN] $HOST_NAME: tomcat not running, who care?"
    fi
}


if [ $SERVER_TYPE = "jboss" ]; then
    stop_jboss
fi

if [ $SERVER_TYPE = "tomcat" ]; then
    stop_tomcat
fi
