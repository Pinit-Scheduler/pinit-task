# RabbitMQ Testcontainers 통합 테스트 런북

## 목적

- RabbitMQ Testcontainers 기반 통합 테스트를 로컬/CI에서 동일한 방식으로 실행하고, 실패 시 빠르게 원인을 진단한다.

## 사전 점검

1. Docker 상태 확인

```bash
docker info
```

2. Java/Gradle 확인

```bash
java -version
./gradlew --version
```

3. 테스트 프로파일 확인

- `src/test/resources/application-test.yml`이 로드되는지 확인한다.

## 실행 방법

1. 단일 통합 테스트 실행

```bash
./gradlew test --tests "*ScheduleStateRabbitPublishIntegrationTest"
```

2. 전체 테스트 실행

```bash
./gradlew test
```

## Docker 미가용 정책

- 테스트 코드는 Docker 미가용 시 `Assumptions.assumeTrue`로 skip 처리한다.
- skip 사유는 테스트 로그에 출력된다.
- CI에서는 테스트 전 `docker info`를 실행해 환경 문제를 조기 감지한다.

## 실패 시 1차 진단 순서

1. Docker 동작 확인: `docker info`
2. Testcontainers 로그 확인: `build/testcontainers-logs/rabbitmq-container.log`
3. 테스트 리포트 확인:
    - `build/test-results/test`
    - `build/reports/tests/test`
4. RabbitMQ 설정 확인:
    - exchange: `task.schedule.direct`
    - routing key: `schedule.state.started`, `schedule.state.completed`, `schedule.state.canceled`
    - `@DynamicPropertySource` 주입 값(host/port/username/password)
5. 큐 바인딩/정리 확인:
    - 테스트 큐 생성 및 바인딩
    - purge/drain 후 검증 시작 여부

## flaky 판정 기준

- 동일 커밋에서 동일 테스트를 3회 재실행했을 때 1회 이상 timeout/수신 실패가 발생하면 flaky 후보로 분류한다.
- flaky 후보는 아래 항목을 우선 점검한다.
    - 비동기 타임아웃 상수 적정성
    - 큐 정리 누락 여부
    - CI 러너의 Docker/RabbitMQ 리소스 상태

## CI 아티팩트

- 테스트 실패 시 아래 산출물을 보존한다.
    - `build/test-results/test/**`
    - `build/reports/tests/test/**`
    - `build/testcontainers-logs/**`
