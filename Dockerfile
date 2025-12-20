# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 비루트 실행(쿠버네티스 securityContext와도 정합성 좋음)
RUN useradd -r -u 10001 -g root appuser \
  && mkdir -p /app \
  && chown -R 10001:0 /app

# GitHub Actions에서 ./gradlew clean generateProto test build 로 생성된 산출물 사용
# (주의) build/libs 에 plain.jar 와 bootJar가 같이 생길 수 있어 bootJar를 선택하도록 처리
COPY build/libs/*.jar /app/

RUN set -eux; \
  JAR="$(ls /app/*.jar | grep -v -- '-plain\.jar$' | head -n 1)"; \
  mv "$JAR" /app/app.jar; \
  rm -f /app/*-plain.jar || true; \
  chown 10001:0 /app/app.jar

USER 10001

EXPOSE 8080
EXPOSE 9090

# JVM 옵션은 Kubernetes 매니페스트에서 JAVA_TOOL_OPTIONS로 주입 권장
ENTRYPOINT ["java","-jar","/app/app.jar"]
