#!/bin/sh  
. /etc/profile

APP_MAIN=moa-report-service
APP_JAR_NAME=moa-report-service.jar
APP_PATH=/opt/moa/moa-report-service/

cd $APP_PATH

tradePortalPID=0

getTradeProtalPID(){  
    javaps=`ps -ef | grep $APP_JAR_NAME | grep -v grep `  
    if [ -n "$javaps" ]; then  
        tradePortalPID=`echo $javaps | awk '{print $2}'`  
    else  
        tradePortalPID=0  
    fi  
}

status(){
	getTradeProtalPID  
	if [ $tradePortalPID -ne 0 ]; then  
        echo "$APP_MAIN already started(PID=$tradePortalPID)"  
        echo "================================================================================================================"  
    else  
        echo "$APP_MAIN is not running"  
        echo "================================================================================================================"  
    fi  
}

start(){  
    getTradeProtalPID  
    CONFIG_PATH=--spring.config.location=
	
	for tradePortalConfigFile in config/*.properties;
	do
	   CONFIG_PATH="$CONFIG_PATH""file:$tradePortalConfigFile,"
	done
	
    echo "================================================================================================================"  
    if [ $tradePortalPID -ne 0 ]; then  
        echo "$APP_MAIN already started(PID=$tradePortalPID)"  
        echo "================================================================================================================"  
    else  
        echo -n "Starting $APP_MAIN"  
        sh -c "cd $APP_PATH"
        sh -c "nohup java -jar $APP_JAR_NAME $CONFIG_PATH >/dev/null 2>&1 & "
        getTradeProtalPID  
        if [ $tradePortalPID -ne 0 ]; then  
            echo "(PID=$tradePortalPID)...[Success]"  
            echo "================================================================================================================"  
        else  
            echo "[Failed]"  
            echo "================================================================================================================"  
        fi  
    fi  
} 

stop(){  
    getTradeProtalPID  
    echo "================================================================================================================"  
    if [ $tradePortalPID -ne 0 ]; then  
        echo -n "Stopping $APP_MAIN(PID=$tradePortalPID)..."  
        kill -9 $tradePortalPID  
        if [ $? -eq 0 ]; then  
            echo "[Success]"  
            echo "================================================================================================================"  
        else  
            echo "[Failed]"  
            echo "================================================================================================================"  
        fi  
        getTradeProtalPID  
        if [ $tradePortalPID -ne 0 ]; then  
            shutdown  
        fi  
    else  
        echo "$APP_MAIN is not running"  
        echo "================================================================================================================"  
    fi  
}  


case "$1" in
	start)
    {
        start
    }
    ;;
    stop)
    {
        stop
    }
    ;;
    restart)
    {
        stop
        start
    }
    ;;
    status)
        status
    ;;
    *)
    {
        echo -e "\nUsage: $0 [ start | stop | restart | status]"
        echo ""
    }
    ;;
esac

