FROM registry.int.yammer.com/java-base:jdk-8u91-b14

ENV SERVICE_NAME breakerbox

# Remove this defensive default after https://jira.int.yammer.com/browse/AZ-242
ENV MARATHON_APP_RESOURCE_MEM 256

WORKDIR /home/app-user
RUN mkdir logs

COPY ./docker/vault-token .vault-token
COPY ./docker/jvm.conf ./
COPY ./docker/start.sh ./
COPY ./docker/${SERVICE_NAME}.yml.template ./
COPY ./docker/${SERVICE_NAME}.jar ./${SERVICE_NAME}.jar

CMD ["/home/app-user/start.sh"]