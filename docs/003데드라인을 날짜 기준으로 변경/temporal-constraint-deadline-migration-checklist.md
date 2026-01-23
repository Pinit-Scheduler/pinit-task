# TemporalConstraint `deadline` ZonedDateAttribute 적용 체크리스트

## 사전 이해

- [x] 기존 `deadline`이 `ZonedDateTimeAttribute`로 `LocalDateTime`+`ZoneId`를 저장하며 컬럼이 `deadline_time`, `deadline_zone_id` 임을
  확인.
- [x] 새 `ZonedDateAttribute`는 날짜(`LocalDate`)와 `ZoneOffset`만 저장하므로 **타임존(Region) 정보 손실**과 **시간(시:분:초) 00:00 고정** 부작용을
  문서화.
- [x] API/도메인/리포지터리 로직이 시간 단위 정밀도나 지역 타임존을 기대하는 부분이 있는지 사용처를 전수 조사.

## 도메인 코드 변경

- [x] `TemporalConstraint.deadline` 타입을 `ZonedDateAttribute`로 교체하고 컬럼명을 `deadline_date`, `deadline_offset_id` 등으로 명시.
- [x] 생성자·변경자에서 `ZonedDateAttribute.from`을 사용하도록 수정하고 null 방어/불변성 점검.
- [x] `getDeadline()`이 자정(`00:00`)으로 복원됨을 호출부에 주석 또는 메서드 네이밍으로 명확히 전달.

## 조회·정렬·커서 로직

- [x] `TaskRepository.findNextByCursor` JPQL의 `deadline.dateTime` 참조를 `deadline.date` 기반으로 변경하고 커서 타입을 `LocalDate`로 맞춤.
- [x] `TaskService.encode/decodeCursor` 직렬화 포맷을 날짜 기준으로 업데이트해 클라이언트·링크 공유·페이징 호환성 영향도를 평가.
- [x] 동일 날짜 내 정렬 기준(시간→ID) 변경 시 업무 규칙을 재확인하여 순서 역전 위험이 없는지 검증.

## DTO·API 계약

- [x] API 계약 변경이 필요하면 기존 V1을 보존하고 **새 V2 엔드포인트**를 추가해 전환(문서/라우팅/버전 네고 포함).
- [x] 응답 DTO `DateTimeWithZone`를 유지할지, 날짜 전용 DTO를 도입할지 결정하고 스웨거 스펙/예제를 갱신.
- [ ] 요청·응답에서 시간이 아닌 “마감 날짜” 의미임을 프런트/모바일 팀과 합의하고 배포 노트에 명시.
- [x] `ZoneId` → `ZoneOffset` 변경 시 클라이언트 파싱 호환성(예: `+09:00` 고정 포맷 사용, `Asia/Seoul` 미지원)을 명시하고 샘플 페이로드를 제공.

## 데이터 마이그레이션

- [x] 새 컬럼 DDL 작성 (`deadline_date` DATE, `deadline_offset_id` VARCHAR 등) 및 기존 컬럼 유지/삭제 전략 확정.
- [x] 기존 `deadline_time`, `deadline_zone_id` 값을 날짜와 오프셋으로 변환하는 DML 준비 (`deadline_time::date`, `deadline_zone_id`를 해당 날짜의
  오프셋으로 계산).
- [x] 지역 타임존이 사라지므로 DST 전환일의 오프셋 결정 방식(표준 vs 써머타임)을 명문화하고 샘플 케이스로 검증.
- [x] 롤백 스크립트, 데이터 백업 시점, 장애 시 복구 절차를 마련.

## 테스트

- [x] 도메인 단위 테스트: 생성/equals/patch/커서 비교가 날짜 단위로 동작하는지 추가.
- [x] 리포지터리 통합 테스트: JPQL 정렬·커서 조회가 기대대로 수행되는지 확인.
- [x] API 컨트롤러/스냅샷 테스트: 직렬화 포맷 변경으로 응답이 깨지지 않는지 검증.
- [ ] 마이그레이션 스크립트(Flyway/Liquibase) 실행 테스트 포함.

## 배포 체크

- [ ] 배포 전에 클라이언트·배치·알림 시스템에 “시간 → 날짜” 계약 변경을 공지.
- [ ] 릴리스 노트에 클라이언트는 `+09:00` 같은 `ZoneOffset` 문자열로 파싱해야 함을 명시(V2 API 포함).
- [ ] 배포 순서: DB 마이그레이션 → 애플리케이션 배포 → 캐시/큐/스케줄러 재기동 필요 여부 확인.
- [ ] 문제 발생 시 스키마/애플리케이션 롤백 플랜과 모니터링 포인트를 준비.
