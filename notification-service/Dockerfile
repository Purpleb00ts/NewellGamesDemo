# Dockerfile (multi-stage)
# Stage 1: build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# copy maven config separately for caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 mvn -B -f pom.xml -q -DskipTests dependency:go-offline

# copy sources and package
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -f pom.xml -DskipTests clean package

# Stage 2: runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app

# copy jar produced in build stage
COPY --from=build /app/target/*.jar app.jar

# Install netcat before copying scripts/jar
RUN apt-get update && \
    apt-get install -y netcat-openbsd && \
    rm -rf /var/lib/apt/lists/*

# Copy jar produced in build stage
COPY --from=build /app/target/*.jar app.jar

# Copy wait-for script
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh

EXPOSE 8080

# Use the wait-for script as entrypoint
ENTRYPOINT ["/wait-for.sh", "java", "-jar", "app.jar"]