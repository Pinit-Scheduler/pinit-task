# Testcontainers 기반 통합 테스트 체크리스트

> 목적: 로컬/CI 환경에서 RabbitMQ Testcontainers 통합 테스트를 안정적으로 수행하기 위한 구현/운영 체크리스트를 제공한다.

## 적용 범위

- [x] 적용 대상: RabbitMQ Testcontainers 통합 테스트
- [x] 비적용 범위: 순수 단위 테스트, Mock-only 테스트, RabbitMQ 외 컨테이너(MySQL/Redis/Kafka)

## 사전 준비

- [x] Docker 실행 가능 여부 확인 절차를 문서화 (`docker info`)했다
- [x] 테스트 의존성 확인 (`build.gradle`의 `org.testcontainers:junit-jupiter`, `org.testcontainers:rabbitmq`)
- [x] 테스트 프로파일 분리 확인 (`src/test/resources/application-test.yml`)
- [x] Java 21 런타임 일치 확인 (로컬/CI)
- [x] CI 러너 Docker 접근 권한 점검 단계를 워크플로에 추가했다

## 테스트 설계

- [x] 통합 테스트 경계를 메시징 발행(RabbitMQ)으로 명시했다
- [x] 검증 포인트를 비즈니스 결과(상태 전환별 이벤트 발행/미발행) 기준으로 정의했다
- [x] 테스트 데이터 격리 전략을 반영했다 (테스트별 동적 큐명)
- [x] 시간 의존 로직은 고정 시각(`BASE_TIME`)을 사용한다
- [x] 실패 케이스를 포함한다 (권한 불일치, 잘못된 상태 전이, 라우팅 미바인딩)

## 구현

- [x] `@SpringBootTest` + `@ActiveProfiles("test")` 기준을 지킨다
- [x] 공통 베이스에서 `@DynamicPropertySource`로 런타임 프로퍼티를 주입한다
- [x] 공통 베이스에서 컨테이너 생명주기(start/stop)를 관리한다
- [x] Docker 미가용 환경은 `Assumptions.assumeTrue`로 skip 처리하고 사유를 로그에 남긴다
- [x] 테스트 리소스(큐/바인딩) 초기화/정리(선언, purge/drain, delete)를 명시적으로 수행한다
- [x] 비동기 검증 타임아웃 상수를 표준화했다 (`MESSAGE_TIMEOUT_MS`, `NO_MESSAGE_TIMEOUT_MS`)
- [x] 테스트 간 상태 누수 방지 절차를 적용했다 (DB 삭제, 큐 purge, `DomainEvents` clear)
- [x] 컨테이너 로그를 `build/testcontainers-logs/rabbitmq-container.log`로 수집한다

## 실행 및 확인

- [x] 단일 통합 테스트 실행 방법을 문서화한다

```bash
./gradlew test --tests "*ScheduleStateRabbitPublishIntegrationTest"
```

- [x] 전체 테스트 실행 방법을 문서화한다

```bash
./gradlew test
```

- [x] 실패 시 1차 진단 순서를 런북에 명시했다 (Docker -> 컨테이너 로그 -> 바인딩/프로퍼티)
- [x] flaky 판단 기준(동일 커밋 재실행 편차/타임아웃 빈도)을 런북에 명시했다

## CI 파이프라인

- [x] CI에서 Docker 가용성 점검(`docker info`)을 수행한다
- [x] 기존 `./gradlew ... test ...` 흐름에 통합 테스트를 포함한다
- [x] 테스트 실패 시 리포트/로그 아티팩트를 보존한다
- [x] Gradle 캐시(`actions/setup-java`의 `cache: gradle`)를 유지한다

## 현재 저장소 기준 메모

- [x] Testcontainers 적용 범위는 RabbitMQ 중심이다
- [x] 참조 테스트 클래스:
  `src/test/java/me/gg/pinit/pinittask/infrastructure/events/schedule/ScheduleStateRabbitPublishIntegrationTest.java`
- [x] 공통 베이스 클래스: `src/test/java/me/gg/pinit/pinittask/infrastructure/events/support/RabbitMqTestcontainersSupport.java`
- [x] 관련 의존성: `build.gradle`
- [x] 테스트 프로파일: `src/test/resources/application-test.yml`
- [x] 실행/진단 런북: `docs/006 Testcontainers 통합 테스트/rabbitmq-testcontainers-runbook.md`

## 완료 기준

- [x] 신규 RabbitMQ 통합 테스트 추가 시 본 체크리스트만으로 PR 준비가 가능하다
- [x] 로컬/CI 공통 실패 원인 진단 절차가 문서에 포함되어 있다
- [x] 실제 케이스(RabbitMQ 상태 이벤트 발행 테스트)에 즉시 적용 가능하다
