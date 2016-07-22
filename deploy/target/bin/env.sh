#!/bin/bash
#@Authro Lijian
#@Date 2007-05-24

BASE_BIN_DIR=`dirname $0`
. $BASE_BIN_DIR/functions.sh

export JAVA_HOME=$yoho.env.javahome
export WEB_APP_HOME=/home/master/yohobuy-order/deploy
export SERVER_PORT=4
export PRODUCTION_MODE="$yoho.env.productmode"
export SERVER_HOME=$yoho.env.serverhome
export SERVER_TYPE=$yoho.env.servertype
export SERVER_NAMESPACE=order
export GW_URL=$Gateway_URL



export NAMING_PORT=`expr 9000 + $SERVER_PORT`
export HTTP_SERVER_PORT=`expr 8080 + $SERVER_PORT`

export CHECK_SERVER_STARTUP_URL="http://127.0.0.1:$HTTP_SERVER_PORT/$SERVER_NAMESPACE/common/ok.jsp"
export STARTUP_SUCCESS_MSG="Server Online Resources ok"
export JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
export JAVA_DEBUG_OPT=" -server -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=,server=y,suspend=n "
export TIGER_JMX_OPT=" -Dcom.sun.management.config.file=$WEB_APP_HOME/conf/jmx/jmx_monitor_management.properties "

export JMX_PORT=`expr 9600 + $SERVER_PORT`
export LOCAL_IP=`/sbin/ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:"|awk 'NR==1 {print}'`
export CATALINA_OPTS="$CATALINA_OPTS -Djava.rmi.server.hostname=$LOCAL_IP -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"


GC_LOGS_DIR=/Data/logs/gc/order
HEAPDUMP_LOGS_DIR=/Data/logs/heapDump/order
if [ ! -d $GC_LOGS_DIR ]; then
   mkdir -p $GC_LOGS_DIR
fi

if [ ! -d $HEAPDUMP_LOGS_DIR ]; then
   mkdir -p $HEAPDUMP_LOGS_DIR
fi

if [ ! -e $JAVA_HOME ]; then
    failed "********************************************************************"
    failed "**Error: JAVA_HOME $JAVA_HOME not exist"
    failed "********************************************************************"    
    exit 1
fi

if [ ! -e $SERVER_HOME ]; then
    failed "********************************************************************"
    failed "**Error: SERVER_HOME $SERVER_HOME not exist."
    failed "********************************************************************"
    exit 1
fi
#========end enviroment var define


if [ $PRODUCTION_MODE = "PRODUCT" ]; then
    #after the environment of online is all 64-bit,the below if and else judgement can be delete
    str=`file $JAVA_HOME/bin/java | grep 64-bit`
    if [ -n "$str" ]; then
        let memTotal=`cat /proc/meminfo |grep MemTotal|awk '{printf "%d", $2/1024 }'`
        if [ $memTotal -gt 10000 ];then
            JAVA_MEM_OPTS=" -server -Xmx4000M -Xms4000M -Xmn600M -XX:PermSize=200M -XX:MaxPermSize=200M -Xss256K -XX:+DisableExplicitGC -XX:SurvivorRatio=1 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 -XX:+CMSClassUnloadingEnabled -XX:LargePageSizeInBytes=128M -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80 -XX:SoftRefLRUPolicyMSPerMB=0 -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -Xloggc:$GC_LOGS_DIR/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$HEAPDUMP_LOGS_DIR/heapdump.hprof "
        else 
            JAVA_MEM_OPTS=" -server -Xmx1g -Xms1g -Xmn256m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=64m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "
        fi
    else
        JAVA_MEM_OPTS=" -server -Xms1024m -Xmx1024m -XX:PermSize=128m -XX:SurvivorRatio=2 -XX:+UseParallelGC "
    fi
        
    JAVA_OPTS=" $JAVA_OPTS $JAVA_MEM_OPTS "
     
elif [ $PRODUCTION_MODE = "TEST" ]; then
    JAVA_MEM_OPTS=" -server -Xms1024m -Xmx1024m -XX:MaxPermSize=128m -XX:SurvivorRatio=2 -XX:+UseParallelGC "
    JAVA_OPTS=" $JAVA_OPTS $JAVA_MEM_OPTS "
elif [ $PRODUCTION_MODE = "DEV" ]; then
    #we shuold reduce resource usage on developing mode
    JAVA_MEM_OPTS=" -server -Xms64m -Xmx1024m -XX:MaxPermSize=128m "
    JAVA_OPTS=" $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPT "
else
    failed "********************************************************************"
    failed "**Error: \$PRODUCTION_MODE should be only: run, test, dev"
    failed "********************************************************************"    
    exit 1
fi


JAVA_OPTS=" $JAVA_OPTS $SERVER_BASE_DIR "
