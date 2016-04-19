#!/bin/bash
#@auther 

usage()
{
  echo "Usage: ${0##*/} [-d] {start|stop} "
  exit 1
}

export JAVA_HOME=/usr/local/jdk
PATH=/usr/local/jdk/bin:$PATH

[ $# -gt 0 ] || usage 

DEBUG=0
while [[ $1 = -* ]]; do
  case $1 in
     -d) DEBUG=1 ;;
  esac
  shift
done
ACTION=$1
shift

cd /data0/logpush

case "$ACTION" in
  start)
    echo "Starting logpush ... "
    rsync 10.77.96.122::logpush/logpush-0.0.1-jar-with-dependencies.jar ./
    nohup java -cp logpush-0.0.1-jar-with-dependencies.jar com.sailing.Sailing > error.log 2>&1 &
    pid=`jps|grep Sailing|awk '{print $1}'`
    echo "running pid:$pid "	
  ;;
  
  stop)
    echo "Stoping logpush ... "
    pid=`jps|grep Sailing|awk '{print $1}'` 
    kill -9  $pid
    echo "stop pid: $pid "     
  ;;
esac

exit 0
