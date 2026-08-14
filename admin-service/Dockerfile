# Build depuis la racine du repo :
#   docker build -f admin-service/Dockerfile -t findme/admin-service .
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY address-service/pom.xml address-service/pom.xml
COPY admin-service/pom.xml admin-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY .mvn .mvn
COPY mvnw .

RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw && ./mvnw -q -pl admin-service -am dependency:go-offline

COPY admin-service admin-service
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q -pl admin-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/admin-service/target/admin-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]