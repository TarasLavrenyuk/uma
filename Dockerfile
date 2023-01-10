FROM gradle:jdk17-alpine
ARG JAR_FILE=build/libs/uma-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
