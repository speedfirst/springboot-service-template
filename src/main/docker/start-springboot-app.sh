#!/bin/sh
APP_NAME=${project.name}

if [ "x$JAVA_COMMON_OPTS" = "x" ]; then
    JAVA_COMMON_OPTS="-server -Djava.awt.headless=true -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8"
fi
JAVA_OPTS="$JAVA_OPTS $JAVA_COMMON_OPTS"

if [ "x$JAVA_HEAP_OPTS" = "x" ]; then
    JAVA_HEAP_OPTS="-Xms256m -Xmx512m"
fi
JAVA_OPTS="$JAVA_OPTS $JAVA_HEAP_OPTS"

if [ "x$JAVA_GC_OPTS" = "x" ]; then
    JAVA_GC_OPTS="-XX:+UseConcMarkSweepGC \
                  -XX:+CMSParallelRemarkEnabled \
                  -XX:+UseCMSInitiatingOccupancyOnly \
                  -XX:CMSInitiatingOccupancyFraction=75 \
                  -XX:+ScavengeBeforeFullGC \
                  -XX:+CMSScavengeBeforeRemark"
fi
JAVA_OPTS="$JAVA_OPTS $JAVA_GC_OPTS"

if [ "x$JAVA_GCLOG_OPTS" = "x" ]; then
    JAVA_GCLOG_OPTS="-XX:+PrintGCDateStamps \
                     -verbose:gc \
                     -XX:+PrintGCDetails \
                     -Xloggc:log/gc.log \
                     -XX:+UseGCLogFileRotation \
                     -XX:NumberOfGCLogFiles=10 \
                     -XX:GCLogFileSize=100m"
fi
JAVA_OPTS="$JAVA_OPTS $JAVA_GCLOG_OPTS"

if [ "x$APP_DEBUG" != "x" ]; then
    # debug port must be 5005
    JAVA_DEBUG_OPTS="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n"
fi

if [ "x$JAVA_DEBUG_OPTS" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS $JAVA_DEBUG_OPTS"
fi


if [ "x$APP_JMX" != "x" ]; then
    # jmx port must be 5055
    JAVA_JMX_OPTS="-Dcom.sun.management.jmxremote.port=5055 \
                   -Dcom.sun.management.jmxremote.authenticate=false \
                   -Dcom.sun.management.jmxremote.ssl=false"
fi

if [ "x$JAVA_JMX_OPTS" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS $JAVA_JMX_OPTS"
fi

RUN_CMD="java $JAVA_OPTS -jar /app/${project.build.finalName}.jar"

if [ "x$APP_OPTS" != "x" ]; then
    RUN_CMD="$RUN_CMD $APP_OPTS"
fi

echo ================================== $APP_NAME app start ============================
echo $RUN_CMD

exec $RUN_CMD
