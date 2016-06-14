#!/bin/bash
set -e # stop this script on first error

echo "dw" > logs/${SERVICE_NAME}.log.type \
  && echo "dw" > logs/requests.log.type

templie \
  -T $(cat .vault-token) \
  -v secret/services/$SERVICE_NAME \
  -t $SERVICE_NAME.yml.template \
  -p . \
  -f $SERVICE_NAME.yml

# Use 90% of the memory in the container, floored to nearest MB
HEAP=$(($MARATHON_APP_RESOURCE_MEM * 9 / 10))

# This converts jvm.conf into a long string of command line arguments
JVM_OPTS=$(sed -e '/^[ ]*\/\//d' -e 's|[ ]*//.*| |' -e 's|^| |' jvm.conf | tr -d "\n")

java \
  -Xms${HEAP}m \
  -Xmx${HEAP}m \
  ${JVM_OPTS} \
  -jar ${SERVICE_NAME}.jar \
  server \
  ${SERVICE_NAME}.yml
