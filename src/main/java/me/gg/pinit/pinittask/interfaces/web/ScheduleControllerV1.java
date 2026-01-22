package me.gg.pinit.pinittask.interfaces.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleStateChangeService;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.interfaces.dto.ScheduleSimplePatchRequest;
import me.gg.pinit.pinittask.interfaces.dto.ScheduleSimpleRequest;
import me.gg.pinit.pinittask.interfaces.dto.ScheduleSimpleResponse;
import me.gg.pinit.pinittask.interfaces.exception.ErrorResponse;
import me.gg.pinit.pinittask.interfaces.utils.MemberId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "ScheduleV1", description = "작업과 분리된 일정 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "현재 상태와 충돌했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class ScheduleControllerV1 {
    private final DateTimeUtils dateTimeUtils;
    private final ScheduleService scheduleService;
    private final ScheduleStateChangeService scheduleStateChangeService;

    @PostMapping
    @Operation(summary = "일정 생성 (작업 없이)", description = "작업과 연결하지 않는 단순 일정을 등록합니다.")
    public ResponseEntity<ScheduleSimpleResponse> createSchedule(@Parameter(hidden = true) @MemberId Long memberId,
                                                                 @Valid @RequestBody ScheduleSimpleRequest request) {
        Schedule saved = scheduleService.addSchedule(request.toSchedule(memberId, dateTimeUtils));
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleSimpleResponse.from(saved));
    }

    @GetMapping
    @Operation(summary = "일정 목록 조회 (작업 없이)", description = "지정한 날짜의 일정을 조회합니다.")
    public List<ScheduleSimpleResponse> getSchedules(@Parameter(hidden = true) @MemberId Long memberId,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                     @RequestParam ZoneId zoneId) {
        List<Schedule> schedules = scheduleService.getScheduleList(memberId, dateTimeUtils.toZonedDateTime(time, zoneId));
        return schedules.stream()
                .map(ScheduleSimpleResponse::from)
                .toList();
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 단건 조회 (작업 없이)")
    public ScheduleSimpleResponse getSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId) {
        Schedule schedule = scheduleService.getSchedule(memberId, scheduleId);
        return ScheduleSimpleResponse.from(schedule);
    }

    @PatchMapping("/{scheduleId}")
    @Operation(summary = "일정 수정 (작업 없이)")
    public ResponseEntity<ScheduleSimpleResponse> updateSchedule(@Parameter(hidden = true) @MemberId Long memberId,
                                                                 @PathVariable Long scheduleId,
                                                                 @RequestBody @Valid ScheduleSimplePatchRequest request) {
        Schedule updated = scheduleService.updateSchedule(memberId, scheduleId, request.toPatch(dateTimeUtils));
        return ResponseEntity.ok(ScheduleSimpleResponse.from(updated));
    }

    @PostMapping("/{scheduleId}/start")
    @Operation(summary = "일정 시작 (작업 없이)")
    public ResponseEntity<Void> startSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                              @RequestParam ZoneId zoneId) {
        scheduleStateChangeService.startSchedule(memberId, scheduleId, dateTimeUtils.toZonedDateTime(time, zoneId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/complete")
    @Operation(summary = "일정 완료 (작업 없이)")
    public ResponseEntity<Void> completeSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                 @RequestParam ZoneId zoneId) {
        scheduleStateChangeService.completeSchedule(memberId, scheduleId, dateTimeUtils.toZonedDateTime(time, zoneId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/suspend")
    @Operation(summary = "일정 일시중지 (작업 없이)")
    public ResponseEntity<Void> suspendSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                @RequestParam ZoneId zoneId) {
        scheduleStateChangeService.suspendSchedule(memberId, scheduleId, dateTimeUtils.toZonedDateTime(time, zoneId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/cancel")
    @Operation(summary = "일정 취소 (작업 없이)")
    public ResponseEntity<Void> cancelSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId) {
        scheduleStateChangeService.cancelSchedule(memberId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제 (작업 없이)")
    public ResponseEntity<Void> deleteSchedule(@Parameter(hidden = true) @MemberId Long memberId,
                                               @PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(memberId, scheduleId);
        return ResponseEntity.noContent().build();
    }
}
