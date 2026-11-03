# 构建阶段：在容器内打出可执行 jar，使用者无需安装 JDK / Gradle
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
# 只配置 :server，跳过需要 Android SDK 的 :phonebox
# 产物统一落成 /tmp/app.jar，排除 -plain.jar，切换版本号不影响本文件
RUN chmod +x gradlew \
 && ./gradlew :server:bootJar --no-daemon --configure-on-demand \
 && cd server/build/libs \
 && for f in *.jar; do case "$f" in *-plain.jar) ;; *) cp "$f" /tmp/app.jar ;; esac; done

# 运行阶段：只保留 jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
