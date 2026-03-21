# 第一阶段：构建
FROM maven:3.8.8-openjdk-11 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true

# 第二阶段：运行
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /app/logs
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]