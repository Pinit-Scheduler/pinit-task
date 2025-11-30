package me.gg.pinit.pinittask.interfaces.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleAdjustmentService;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleStateChangeService;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.interfaces.dto.ScheduleRequest;
import me.gg.pinit.pinittask.interfaces.dto.ScheduleResponse;
import me.gg.pinit.pinittask.interfaces.exception.ErrorResponse;
import me.gg.pinit.pinittask.interfaces.utils.MemberId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "현재 상태와 충돌했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final ScheduleAdjustmentService scheduleAdjustmentService;
    private final ScheduleStateChangeService scheduleStateChangeService;

    @PostMapping
    @Operation(summary = "일정 생성", description = "새 일정과 의존 관계를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "일정이 성공적으로 생성되었습니다."),
            @ApiResponse(responseCode = "400", description = "요청 값 검증에 실패했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ScheduleResponse> createSchedule(@MemberId Long memberId,
                                                           @Valid @RequestBody ScheduleRequest request) {
        Schedule saved = scheduleAdjustmentService.createSchedule(memberId, request.toCommand(null, memberId));
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleResponse.from(saved));
    }

    @PatchMapping("/{scheduleId}")
    @Operation(summary = "일정 수정", description = "일정 본문과 의존 관계를 함께 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일정이 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "요청 값 검증에 실패했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ScheduleResponse> updateSchedule(@MemberId Long memberId,
                                                           @PathVariable Long scheduleId,
                                                           @Valid @RequestBody ScheduleRequest request) {
        Schedule updated = scheduleAdjustmentService.adjustSchedule(memberId, request.toCommand(scheduleId, memberId));
        return ResponseEntity.ok(ScheduleResponse.from(updated));
    }

    @GetMapping
    @Operation(summary = "일정 목록 조회", description = "지정한 날짜의 일정을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회에 성공했습니다."),
            @ApiResponse(responseCode = "400", description = "날짜 형식이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ScheduleResponse> getSchedules(@MemberId Long memberId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.getScheduleList(memberId, date).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 단건 조회", description = "특정 일정의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "단건 조회에 성공했습니다."),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ScheduleResponse getSchedule(@MemberId Long memberId, @PathVariable Long scheduleId) {
        Schedule schedule = scheduleService.getSchedule(memberId, scheduleId);
        return ScheduleResponse.from(schedule);
    }

    @GetMapping("/week")
    @Operation(summary = "주간 일정 조회", description = "해당 주의 일정을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주간 일정 조회에 성공했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ScheduleResponse> getWeeklySchedules(@MemberId Long memberId,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime time) {
        return scheduleService.getScheduleListForWeek(memberId, time).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @PostMapping("/{scheduleId}/start")
    @Operation(summary = "일정 시작", description = "일정을 진행 중 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 시작되었습니다."),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> startSchedule(@MemberId Long memberId, @PathVariable Long scheduleId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime time) {
        scheduleStateChangeService.startSchedule(memberId, scheduleId, time);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/complete")
    @Operation(summary = "일정 완료", description = "진행 중인 일정을 완료 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 완료되었습니다."),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> completeSchedule(@MemberId Long memberId, @PathVariable Long scheduleId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime time) {
        scheduleStateChangeService.completeSchedule(memberId, scheduleId, time);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/suspend")
    @Operation(summary = "일정 일시중지", description = "진행 중인 일정을 일시중지합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 일시중지되었습니다."),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> suspendSchedule(@MemberId Long memberId, @PathVariable Long scheduleId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime time) {
        scheduleStateChangeService.suspendSchedule(memberId, scheduleId, time);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/cancel")
    @Operation(summary = "일정 취소", description = "예정되었거나 진행 중인 일정을 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 취소되었습니다."),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> cancelSchedule(@MemberId Long memberId, @PathVariable Long scheduleId) {
        scheduleStateChangeService.cancelSchedule(memberId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제", description = "지정한 일정을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 삭제되었습니다."),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteSchedule(@MemberId Long memberId, @PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(memberId, scheduleId);
        return ResponseEntity.noContent().build();
    }
}
