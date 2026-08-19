# Build depuis la racine du repo :
#   docker build -t findme/backend .
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN --mount=type=cache,target=/root/.m2/repository chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2/repository ./mvnw -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/findme-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
