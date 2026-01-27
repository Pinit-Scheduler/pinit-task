# 지난 Task 모아보기(완료 + 마감일 1일 경과) API 체크리스트

> 목적: 오늘 00:00 기준 이전(어제까지) 마감일이 지난 완료 Task를 커서 기반으로 모아볼 수 있는 클라이언트용 엔드포인트를 추가한다.

## 요구 정의

- [x] "1일" 기준을 확정한다: **사용자/요청 오프셋의 LocalDate 기준 now-1일 00:00** 을 컷오프로 사용한다.
- [x] 포함 범위를 정의한다: 일정 연결 여부와 무관하게 해당 회원의 모든 완료 Task(마감 필수) 포함.
- [x] 정렬 방향과 기본/최대 페이지 크기를 확정한다: `deadline_date DESC, id DESC`, default 20, max 100, 커서 `yyyy-MM-dd[+HH:mm]|taskId`.
- [x] 응답 필드 확정: `TaskResponseV2` 그대로 사용(마감 날짜+오프셋 노출, 완료 소스는 미노출로 합의).
- [x] 기존 Task 목록 API와의 역할 분리를 명문화한다: `/v2/tasks/completed` 는 마감 1일 경과 + 완료만, 기존 목록은 최신/진행중 포함.

## 스키마 & 도메인

- [x] 도메인에서 마감일(`TemporalConstraint.deadline`)을 손쉽게 `LocalDate`로 얻을 수 있는 헬퍼 확인(기존 `getDeadlineDate()` 재사용).
- [x] 1일 컷오프 계산 시 오프셋을 명시하고 서비스 책임으로 분리(`TaskArchiveService`, 테스트용 Clock 주입).
- [x] DB 인덱스 필요 여부를 검토한다: `(owner_id, completed, deadline_date, task_id)` 복합 인덱스 추가용 DDL 초안 작성(
  `task-archive-index.sql`).

## 레포지토리 & 쿼리

- [x] `TaskRepository`에 완료 Task 전용 커서 쿼리를 추가한다: `completed=true`, `deadline_date <= cutoffDate`, 정렬
  `deadline_date DESC, id DESC`.
- [x] 커서 인코딩/디코딩 포맷을 정의한다(`yyyy-MM-dd|{id}` 기본, 오프셋 허용) 및 파싱 실패 예외 메시지를 규격화한다.
- [x] 커서 단위 테스트/통합 테스트로 동일 마감일 + ID 타이브레이킹이 안정적으로 동작하는지 검증한다.

## 서비스 & 애플리케이션 레이어

- [x] 1일 컷오프 계산 방식을 구현한다(요청/회원 오프셋 기준 LocalDate.now-1) 및 Clock 주입으로 테스트 가능.
- [x] `TaskArchiveService`에 아카이브용 커서 페이지 모델을 추가하고 DESC 정렬에 맞는 `hasNext`/`nextCursor`(size+1) 계산 완료.
- [x] 완료→재오픈 시 아카이브 대상에서 제외되도록 completed 플래그 의존(재오픈 시 false 처리 유지).

## API 계약 & 컨트롤러

- [x] V2에 `GET /v2/tasks/completed` 커서 엔드포인트 추가, `size(1~100)`, `cursor`, `offset` 검증 처리.
- [x] 응답 DTO는 `TaskArchiveCursorPageResponseV2` + `TaskResponseV2` 조합으로 확정.
- [x] Swagger/OAS 어노테이션에 커서 예제, 1일 컷오프 설명, 400 응답 반영.
- [x] Member 인증(`@MemberId`) 적용, 별도 레이트리밋/캐싱은 도입하지 않기로 결정.

## 테스트

- [x] 도메인/서비스 단위: 오프셋 기반 컷오프·커서 파싱·size 검증 테스트 추가.
- [x] 리포지토리 통합: 커서 정렬/필터/경계 테스트 추가.
- [x] 서비스 단위: 커서 직렬화/역직렬화, 잘못된 커서 예외, size 상한, cutoff 계산 검증.
- [x] 컨트롤러: 요청 검증(0 size), 커서 페이지 동작, 오프셋 파라미터 적용 통합 테스트.
- [x] 이벤트/로그: 별도 트래킹 미도입으로 N/A 확인.

## 배포 & 운영

- [x] 스키마 변경 없음(인덱스만 추가)으로 확정, 적용 DDL은 `task-archive-index.sql`에 기록.
- [x] 운영/스테이징은 수동 인덱스 적용 전제(`ddl-auto` 유지 시 위험 없도록 별도 적용 계획 필요).
- [x] 모니터링 포인트 제안: archive API latency/에러율, 응답 건수, deadline null 비율 알림.
- [x] 프런트/모바일 공지 사항 정리: 커서 포맷·1일 기준 오프셋·max size 100·응답 필드 기존과 동일(새 엔드포인트) 공유 필요.
