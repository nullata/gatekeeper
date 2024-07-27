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

JAR_FILE=/gatekeeper/Gatekeeper-0.0.1-SNAPSHOT.jar

LOG_TS=$(date +%d-%m-%Y--%H-%M-%S)
LOG_FILE=/var/log/gatekeeper/${LOG_TS}--gatekeeper.log

if ! [[ -f "${JAR_FILE}" ]];then
    echo "Jar file could not be located at: ${JAR_FILE}, exiting." | tee -a "${LOG_FILE}"
    exit 1
fi

echo "Starting Gatekeeper with PROXY_TARGET_URL=\"${PROXY_TARGET_URL}\"" | tee -a "${LOG_FILE}"
java -DPROXY_TARGET_URL="${PROXY_TARGET_URL}" -jar "${JAR_FILE}" | tee -a "${LOG_FILE}"
