# syntax=docker/dockerfile:1

# ── Stage 1: build with Maven (cache-friendly)
FROM maven:3.9.6-eclipse-temurin-11 AS builder
WORKDIR /app

# go-offline to cache dependencies
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# build sources
COPY src ./src
RUN mvn -q clean package -DskipTests

# ── Stage 2: lean runtime with Temurin JRE 11 (alpine)
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

# if you bump project version, update this ARG or override at build time
ARG JAR_NAME=number-range-summarizer-1.0.0.jar

# copy the thin, runnable jar (manifest has Main-Class already)
COPY --from=builder /app/target/${JAR_NAME} /app/app.jar

# non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# default entrypoint runs the CLI; args after image name are passed to the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
