#!/bin/bash

function checkPackage {
    local packageName=$1
    if [[ -z "${packageName// /}" ]];then
        echo "package name not specified in ${FUNCNAME[0]}"
        exit 1
    fi

    if [[ -z $(which "${packageName// /}") ]];then
        echo "${packageName} package missing"
        exit 1
    fi
}

checkPackage mvn
checkPackage docker
checkPackage docker-compose

echo "Packaging jar file"
mvn clean package
echo "Deploying application"
docker-compose up --build -d
