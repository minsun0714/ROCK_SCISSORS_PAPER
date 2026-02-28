########################################
# 1️⃣ Build Stage
########################################
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test


########################################
# 2️⃣ Runtime Stage
########################################
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
ENV TZ=Asia/Seoul

# 필수 패키지 최소 설치
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata curl && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    groupadd -r app && useradd -r -g app app && \
    mkdir -p /app/logs && \
    chown -R app:app /app && \
    rm -rf /var/lib/apt/lists/*

# 빌드 산출물 복사
COPY --from=builder --chown=app:app /workspace/build/libs/*.jar /app/app.jar

USER app

########################################
# 3️⃣ JVM 튜닝
########################################
ENV JAVA_TOOL_OPTIONS="\
-Xms1g \
-Xmx1g \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:MaxMetaspaceSize=256m \
-XX:MaxDirectMemorySize=256m \
-Xlog:gc*:stdout:time,uptime,level,tags \
-Duser.timezone=Asia/Seoul \
-Dfile.encoding=UTF-8"

########################################
# 4️⃣ 실행
########################################
ENTRYPOINT ["java","-jar","/app/app.jar"]

########################################
# 5️⃣ Health Check
########################################
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1