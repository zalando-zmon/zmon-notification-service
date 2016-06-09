FROM registry.opensource.zalan.do/stups/openjdk:8-26

EXPOSE 8080

COPY target/zmon-notification-service-0.0.1-SNAPSHOT.jar /zmon-notification-service.jar
COPY target/scm-source.json /

CMD java $JAVA_OPTS $(java-dynamic-memory-opts) -jar /zmon-notification-service.jar
