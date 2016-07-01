FROM java:openjdk-8-jre-alpine
MAINTAINER Marco Lamina <marco.lamina@zehndetails.com>

LABEL name="breakerbox" version=0.4.4-SNAPSHOT
RUN mkdir /opt 
ADD breakerbox-service/target/breakerbox-service-0.4.4-SNAPSHOT.jar /opt/

EXPOSE 8080 8081

WORKDIR /opt

ENTRYPOINT ["java", "-jar", "breakerbox-service-0.4.4-SNAPSHOT.jar"]
CMD ["server", "breakerbox.yml"]
