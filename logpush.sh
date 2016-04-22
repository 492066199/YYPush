#!/bin/bash
#@auther 

usage()
{
  echo "Usage: ${0##*/} [-d] {start|stop} "
  exit 1
}

export zookeeper="172.16.89.130:2181,172.16.89.128:2181,172.16.89.129:2181"
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
    nohup java -server -XX:MaxDirectMemorySize=2g -Xmx2g -XX:+UseConcMarkSweepGC -cp logpush-0.0.1-jar-with-dependencies.jar io.uve.yypush.Sailing > error.log 2>&1 &
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
