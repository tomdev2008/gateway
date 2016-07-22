#!/bin/bash
################################################################
#@Authro Lijian
#@Date 2007-05-24
#@Description: 
#   the script is on top level
#   check if root run the script
################################################################

#if [ `id -u` = 0 ]
#then
#    echo "****************************************************"
#    echo "*Error: root (the superuser) can't run this script.*"
#    echo "****************************************************"    
#    exit 1
#fi

cygwin=false
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

if [ $cygwin = true ]
then
    echo "****************************************************"
    echo "*Error: Cygwin shell can't run this script.*********"
    echo "****************************************************"    
    exit 1
fi

if [ -f /etc/sysconfig/init ]; then
    . /etc/sysconfig/init
else
  SETCOLOR_SUCCESS=
  SETCOLOR_FAILURE=
  SETCOLOR_WARNING=
  SETCOLOR_NORMAL=
fi

success () {
    if [ "$BOOTUP" = "color" ]; then
        $SETCOLOR_SUCCESS
        if [ -z "$*" ]; then
            echo "ok"
        else
            echo -e "$*"
        fi
        $SETCOLOR_NORMAL
    else
        if [ -z "$*" ]; then
            echo "ok"
        else
            echo -e "$*"
        fi
    fi
    return
}

failed () {
    if [ "$BOOTUP" = "color" ]; then
        $SETCOLOR_FAILURE
        if [ -z "$*" ]; then
            echo "failed"
        else
            echo -e "$*"
        fi
        $SETCOLOR_NORMAL
    else
        if [ -z "$*" ]; then
            echo "failed"
        else
            echo -e "$*"
        fi
    fi
    return
}

warning () {
    if [ "$BOOTUP" = "color" ]; then
        $SETCOLOR_WARNING
        if [ -z "$*" ]; then
            echo "warning"
        else
            echo -e "$*"
        fi
        $SETCOLOR_NORMAL
    else
        if [ -z "$*" ]; then
            echo "warning"
        else
            echo -e "$*"
        fi
    fi
    return
}

remove_ipcs () {
    pid=$*
    who=`whoami`
    if [ -z $pid ]; then
        warning "Warning: Removed share memory and semaphore--gived pid is NULL"
        return
    fi
    shmids=`ipcs -mp |grep -P "\d+[ ]+$who[ ]+$pid" |awk '{print $1}'`
    for id in $shmids ; do
        ipcrm -m $id
    done

    shsemids=`ipcs -sp |grep -P "\d+[ ]+$who[ ]+$pid" |awk '{print $1}'`
    for id in $shsemids ; do
        ipcrm -s $id
    done    
}

printProcess() {
    times=$*
    if [ -z $times ]; then
        times=1    
    fi 
    
    for (( i=0;  i<$times;  i=i+1 ))
    do
        echo -e ".\c"
        sleep 1 
    done
}


