FROM openjdk:17-jdk-slim

RUN mkdir -p /gatekeeper
RUN mkdir -p /var/log/gatekeeper
WORKDIR /gatekeeper
COPY target/Gatekeeper-0.0.1-SNAPSHOT.jar /gatekeeper/Gatekeeper-0.0.1-SNAPSHOT.jar
COPY entrypoint.sh /gatekeeper/entrypoint.sh
RUN chmod +x /gatekeeper/entrypoint.sh
EXPOSE 8080

ENTRYPOINT ["/gatekeeper/entrypoint.sh"]
