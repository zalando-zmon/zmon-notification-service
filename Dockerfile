FROM registry.opensource.zalan.do/stups/openjdk:latest

EXPOSE 8080

COPY target/zmon-notification-service-0.0.1-SNAPSHOT.jar /zmon-notification-service.jar

CMD java $JAVA_OPTS $(java-dynamic-memory-opts) -jar /zmon-notification-service.jar
