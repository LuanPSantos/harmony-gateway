FROM gradle:6.9.1-jdk11 AS Builder

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle clean build --no-daemon

# use openjdk:11-jre-slim if alpine does not work
FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=Builder /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "./app.jar"]