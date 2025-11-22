package me.gg.pinit.pinittask.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleAdjustmentService;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleStateChangeService;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.infrastructure.web.dto.ScheduleRequest;
import me.gg.pinit.pinittask.infrastructure.web.dto.ScheduleResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/members/{memberId}/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 관리 API")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleAdjustmentService scheduleAdjustmentService;
    private final ScheduleStateChangeService scheduleStateChangeService;

    @PostMapping
    @Operation(summary = "일정 생성", description = "새로운 일정과 의존 관계를 등록합니다.")
    public ResponseEntity<ScheduleResponse> createSchedule(@PathVariable Long memberId,
                                                            @Valid @RequestBody ScheduleRequest request) {
        Schedule saved = scheduleAdjustmentService.createSchedule(memberId, request.toCommand(null, memberId));
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleResponse.from(saved));
    }

    @PostMapping("/{scheduleId}")
    @Operation(summary = "일정 수정", description = "일정 내용과 의존 관계를 함께 수정합니다.")
    public ResponseEntity<ScheduleResponse> updateSchedule(@PathVariable Long memberId,
                                                           @PathVariable Long scheduleId,
                                                           @Valid @RequestBody ScheduleRequest request) {
        Schedule updated = scheduleAdjustmentService.adjustSchedule(memberId, request.toCommand(scheduleId, memberId));
        return ResponseEntity.ok(ScheduleResponse.from(updated));
    }

    @GetMapping
    @Operation(summary = "일정 목록 조회", description = "지정한 날짜의 일정을 조회합니다.")
    public List<ScheduleResponse> getSchedules(@PathVariable Long memberId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.getScheduleList(memberId, date).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 단건 조회", description = "일정 상세 정보를 조회합니다.")
    public ScheduleResponse getSchedule(@PathVariable Long memberId, @PathVariable Long scheduleId) {
        Schedule schedule = scheduleService.getSchedule(memberId, scheduleId);
        return ScheduleResponse.from(schedule);
    }

    @PostMapping("/{scheduleId}/start")
    @Operation(summary = "일정 시작", description = "일정을 시작 상태로 변경합니다.")
    public ResponseEntity<Void> startSchedule(@PathVariable Long memberId, @PathVariable Long scheduleId) {
        scheduleStateChangeService.startSchedule(memberId, scheduleId, ZonedDateTime.now());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/complete")
    @Operation(summary = "일정 완료", description = "진행 중인 일정을 완료 처리합니다.")
    public ResponseEntity<Void> completeSchedule(@PathVariable Long memberId, @PathVariable Long scheduleId) {
        scheduleStateChangeService.completeSchedule(memberId, scheduleId, ZonedDateTime.now());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/suspend")
    @Operation(summary = "일정 일시중지", description = "진행 중인 일정을 일시 중지합니다.")
    public ResponseEntity<Void> suspendSchedule(@PathVariable Long memberId, @PathVariable Long scheduleId) {
        scheduleStateChangeService.suspendSchedule(memberId, scheduleId, ZonedDateTime.now());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/cancel")
    @Operation(summary = "일정 취소", description = "진행 중이거나 예정된 일정을 취소합니다.")
    public ResponseEntity<Void> cancelSchedule(@PathVariable Long memberId, @PathVariable Long scheduleId) {
        scheduleStateChangeService.cancelSchedule(memberId, scheduleId);
        return ResponseEntity.noContent().build();
    }
}
