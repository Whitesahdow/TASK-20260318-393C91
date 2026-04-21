FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

FROM maven:3.9.9-eclipse-temurin-17
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/pom.xml pom.xml
COPY --from=build /app/src src
COPY --from=build /root/.m2 /root/.m2

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
