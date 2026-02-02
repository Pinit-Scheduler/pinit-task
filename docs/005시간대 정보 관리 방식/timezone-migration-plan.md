# 시간대 컬럼/ZoneId 확장 백필 계획 (2026-02-01)

## 대상 및 범위

- `task.deadline_zone_id`: 이미 IANA ZoneId 저장. 추가 컬럼 필요 시 (예: 반복 일정용 `zone_id`) 동일 패턴 적용.
- 추후 반복 일정/리마인더 테이블 신설 시 `zone_id` 및 로컬 정의 컬럼(`local_time`, `day_of_week_set`) 필수.

## 백필 단계

1) **스키마 추가**: 새 `zone_id` 또는 `local_time` 컬럼을 NULL 허용으로 추가. 기본값 지정 금지.
2) **데이터 백필**: 기존 레코드별로 회원 프로필의 `time_zone` 또는 추론 가능한 IANA 값을 넣는다.
    - 예: `task.deadline_zone_id`가 비어있다면 `member.time_zone`로 채움.
3) **검증 쿼리**: NULL/빈 문자열이 남아 있지 않은지 집계.
4) **널 금지 전환**: 애플리케이션 배포 후 컬럼을 `NOT NULL`로 변경.
5) **인덱스/조회 정합성 테스트**: 날짜 범위 + `zone_id` 복합 인덱스가 필요한지 점검.

## 마이그레이션 안전장치

- 롤백 시: 새 컬럼만 drop 하고 기존 컬럼 보존.
- 배포 시점에 애플리케이션은 새 컬럼이 NULL이어도 동작하도록 방어 코드 유지.

## 운영 체크리스트

- 배포 직후 메트릭/로그로 `zone_id` 누락 건 모니터링.
- 데이터 백필 스크립트는 트랜잭션 단위로 커밋해 long-running 잠금 회피.

## 실행 가능한 SQL 예시 (MySQL 8.x, **현재 존재하는 테이블만 사용**)

아래 순서는 `member`, `task`, `statistics`에 이미 존재하는 컬럼을 기준으로 한다. 신규 테이블 생성 없음.

### 1) 컬럼 NULL 허용 상태 점검 및 백필

회원 기본 시간대(ZoneId) 보정 (기본값을 `Asia/Seoul`로 지정):

```sql
UPDATE member
SET zone_id = 'Asia/Seoul'
WHERE zone_id IS NULL
   OR TRIM(zone_id) = '';
```

작업 마감 `deadline_zone_id` 백필 (회원 TZ 사용):

```sql
UPDATE task t
    JOIN member m
ON t.owner_id = m.member_id
SET t.deadline_zone_id = m.zone_id
    WHERE
    (t.deadline_zone_id IS NULL OR TRIM(t.deadline_zone_id) = '')
    AND m.zone_id IS NOT NULL;
```

통계 `start_of_week_zone_id` 백필 (회원 TZ 사용):

```sql
UPDATE statistics s
    JOIN member m
ON s.member_id = m.member_id
SET s.start_of_week_zone_id = m.zone_id
    WHERE
(s.start_of_week_zone_id IS NULL OR TRIM(s.start_of_week_zone_id) = '')
AND m.zone_id IS NOT NULL;
```

### 2) 검증 쿼리 (NULL/공백 잔존 확인)

```sql
SELECT COUNT(*) AS null_member_zone
FROM member
WHERE zone_id IS NULL
   OR TRIM(zone_id) = '';
SELECT COUNT(*) AS null_task_zone
FROM task
WHERE deadline_zone_id IS NULL
   OR TRIM(deadline_zone_id) = '';
SELECT COUNT(*) AS null_stats_zone
FROM statistics
WHERE start_of_week_zone_id IS NULL
   OR TRIM(start_of_week_zone_id) = '';
```

세 값이 모두 0인지 확인.

### 3) NOT NULL 전환

```sql
ALTER TABLE member
    MODIFY COLUMN zone_id VARCHAR (64) NOT NULL;

ALTER TABLE task
    MODIFY COLUMN deadline_zone_id VARCHAR (64) NOT NULL,
    MODIFY COLUMN deadline_offset_id VARCHAR (16) NOT NULL,
    MODIFY COLUMN deadline_date DATE NOT NULL;

ALTER TABLE statistics
    MODIFY COLUMN start_of_week_zone_id VARCHAR (64) NOT NULL,
    MODIFY COLUMN start_of_week_offset_id VARCHAR (16) NOT NULL,
    MODIFY COLUMN start_of_week_date DATE NOT NULL;
```

### 4) 인덱스 점검/보완

- `task`: 이미 `idx_task_owner_deadline (owner_id, deadline_date, task_id)` 존재. 필요 시 ZoneId 포함 복합 인덱스 추가:

```sql
CREATE INDEX idx_task_owner_deadline_zone
    ON task (owner_id, deadline_date, deadline_zone_id);
```

- `statistics`: 주차 집계 조회 최적화:

```sql
CREATE INDEX idx_statistics_member_week_zone
    ON statistics (member_id, start_of_week_date, start_of_week_zone_id);
```

### 5) 롤백 시나리오

```sql
ALTER TABLE statistics
    DROP INDEX idx_statistics_member_week_zone;
ALTER TABLE task
    DROP INDEX idx_task_owner_deadline_zone;
-- NOT NULL을 다시 NULL 허용으로 돌릴 경우 (필요 시만)
ALTER TABLE statistics
    MODIFY COLUMN start_of_week_zone_id VARCHAR (64) NULL,
    MODIFY COLUMN start_of_week_offset_id VARCHAR (16) NULL,
    MODIFY COLUMN start_of_week_date DATE NULL;
ALTER TABLE task
    MODIFY COLUMN deadline_zone_id VARCHAR (64) NULL,
    MODIFY COLUMN deadline_offset_id VARCHAR (16) NULL,
    MODIFY COLUMN deadline_date DATE NULL;
ALTER TABLE member
    MODIFY COLUMN zone_id VARCHAR (64) NULL;
```
