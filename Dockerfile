FROM ubuntu:latest
LABEL authors="shiraku"

# Используем базовый образ с Maven для сборки
FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/target/*.jar  app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]