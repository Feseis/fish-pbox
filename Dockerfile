# 构建阶段：在容器内打出可执行 jar，使用者无需安装 JDK / Gradle
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
# 只配置 :server，跳过需要 Android SDK 的 :phonebox
RUN chmod +x gradlew \
 && ./gradlew :server:bootJar --no-daemon --configure-on-demand

# 运行阶段：只保留 jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/server/build/libs/server-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
