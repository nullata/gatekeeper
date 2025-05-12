#!/bin/bash

function validateVar {
  local varName=$1
  if [[ -z ${!varName// /} ]];then
    echo "${varName} not specified, exiting."
    exit 1
  fi
}

validateVar "PROXY_TARGET_URL"
validateVar "DB_TYPE"
validateVar "DB_HOST"
validateVar "DB_PORT"
validateVar "DB_NAME"
validateVar "DB_USERNAME"
validateVar "DB_PASSWORD"
validateVar "TABLE_NAME"
validateVar "COLUMN_NAME"

JAR_FILE=/gatekeeper/app.jar

LOG_TS=$(date +%d-%m-%Y--%H-%M-%S)
LOG_FILE=/var/log/gatekeeper/${LOG_TS}--gatekeeper.log

if ! [[ -f "${JAR_FILE}" ]];then
    echo "Jar file could not be located at: ${JAR_FILE}, exiting." | tee -a "${LOG_FILE}"
    exit 1
fi

# jgc tune
JAVA_OPTS="
-XX:+UseG1GC 
-XX:MaxGCPauseMillis=200 
-XX:InitiatingHeapOccupancyPercent=45 
-XX:+ParallelRefProcEnabled 
-XX:ConcGCThreads=4 
-XX:ParallelGCThreads=8 
-XX:+UseStringDeduplication 
-XX:+HeapDumpOnOutOfMemoryError 
-XX:HeapDumpPath=/var/log/gatekeeper/heapdump 
-Xlog:gc*:file=/var/log/gatekeeper/gc.log:tags,uptime,time,level
"

echo "Starting Gatekeeper with PROXY_TARGET_URL=\"${PROXY_TARGET_URL}\"" | tee -a "${LOG_FILE}"
java ${JAVA_OPTS} -DPROXY_TARGET_URL="${PROXY_TARGET_URL}" -jar "${JAR_FILE}" | tee -a "${LOG_FILE}"
