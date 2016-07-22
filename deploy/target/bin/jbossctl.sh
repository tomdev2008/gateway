#!/bin/bash
################################################################
#@Authro Lijian
#@Date 2007-05-29
################################################################

BASE_BIN_DIR=`dirname $0`
. $BASE_BIN_DIR/env.sh

TIMESTAMP=`date +%Y_%m_%d_%H_%M`

LOG_DIR=$WEB_APP_HOME/logs
LOGS_SAVED=$LOG_DIR/logs_saved
JBOSS_STDOUT_LOG=$LOG_DIR/jboss_stdout.log
JBOSS_BASE_DIR=$WEB_APP_HOME/.jboss/default

#################################################################
#backup logs
if [ ! -d $LOGS_SAVED ]; then
   mkdir -p $LOGS_SAVED
fi

if [ -f $JBOSS_STDOUT_LOG ]; then
   mv -f $JBOSS_STDOUT_LOG $LOGS_SAVED/jboss_stdout.log.$TIMESTAMP
fi

##check if started before
java_pid=`ps  --no-heading -C java -f --width 1000 | grep "$JBOSS_BASE_DIR" |awk '{print $2}'`
if [ ! -z "$java_pid" ]; then
    echo "Jboss server already started: pid=$java_pid"
    exit;
fi

#########################################copy server home
if [ ! -d "$JBOSS_BASE_DIR" ]; then
    rm -f $JBOSS_BASE_DIR
    mkdir -p $JBOSS_BASE_DIR
    cp -rf $SERVER_HOME/standalone/. $JBOSS_BASE_DIR/.
else
    rm -rf $JBOSS_BASE_DIR/tmp
    rm -rf $JBOSS_BASE_DIR/data
    rm -rf $JBOSS_BASE_DIR/log
fi

deploy

##jmx controle file must be read only else jvm can not startup
chmod 600 $WEB_APP_HOME/conf/jmx/*.properties > /dev/null 2>&1

nohup $SERVER_HOME/bin/standalone.sh


################################################################
copy_jboss_conf(){
    cp -rf $WEB_APP_HOME/conf/jboss/standalone.xml $JBOSS_BASE_DIR/configuration/standalone.xml
}

deploy_ear(){
    rm -rf $JBOSS_BASE_DIR/deployments/web.ear
    rm -rf $JBOSS_BASE_DIR/deployments/web.ear.*
    cp -rf $JBOSS_BASE_DIR/web.ear $JBOSS_BASE_DIR/deployments/web.ear
    touch $JBOSS_BASE_DIR/deployments/web.ear.dodeploy
}

deploy(){
    copy_jboss_conf
    deploy_ear
}