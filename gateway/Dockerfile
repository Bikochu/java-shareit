FROM openjdk:19-jdk-alpine
COPY target/*.jar gateway.jar
ENTRYPOINT ["java","-jar","/gateway.jar"]