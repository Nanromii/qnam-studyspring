FROM openjdk:17

ARG FILE_JAR=target/qnam-studyspring-0.0.1-SNAPSHOT.jar

ADD ${FILE_JAR} api-services.jar

ENTRYPOINT ["java", "-jar", "api-services.jar"]

EXPOSE 8080