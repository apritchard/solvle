#
# Build stage
#
FROM maven:3.8.5-openjdk-18-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package


FROM openjdk:18-alpine
COPY --from=build /home/app/target/*.jar /usr/local/lib/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=80.0", "-jar", "/usr/local/lib/app.jar"]