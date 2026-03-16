# 第一阶段：构建阶段
FROM openjdk:17-jdk-slim AS builder

# 安装 Maven
RUN apt-get update && apt-get install -y maven curl && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 打包应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests -B

# 第二阶段：运行阶段
FROM openjdk:17-jdk-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 创建非 root 用户运行应用
RUN groupadd -r spring && useradd -r -g spring spring

# 设置工作目录
WORKDIR /app

# 从构建阶段复制生成的 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 更改文件所有者为非 root 用户
RUN chown spring:spring app.jar

# 切换到非 root 用户
USER spring:spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
