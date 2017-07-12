FROM docker.travelguru.com/base/tgos-jre8
MAINTAINER Priyadarsh Kankipati <priyadarsh.kankipati@yatra.com>

ADD docker/breakerbox.jar /opt/
ADD breakerbox-service/breakerbox.yml /opt/
ADD breakerbox-service/breakerbox-instances.yml /opt/

EXPOSE 8080 8081

WORKDIR /opt

ENTRYPOINT ["java", "-jar", "breakerbox.jar"]
CMD ["server", "breakerbox.yml"]