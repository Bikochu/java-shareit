FROM openjdk:19-jdk-alpine
COPY target/*.jar server.jar
ENTRYPOINT ["java","-jar","/server.jar"]