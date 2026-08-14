# Build depuis la racine du repo :
#   docker build -f address-service/Dockerfile -t findme/address-service .
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY address-service/pom.xml address-service/pom.xml
COPY admin-service/pom.xml admin-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY .mvn .mvn
COPY mvnw .

RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw && ./mvnw -q -pl address-service -am dependency:go-offline

COPY address-service address-service
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q -pl address-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/address-service/target/address-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]