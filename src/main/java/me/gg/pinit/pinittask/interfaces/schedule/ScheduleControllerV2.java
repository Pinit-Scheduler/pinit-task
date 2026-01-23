package me.gg.pinit.pinittask.interfaces.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import me.gg.pinit.pinittask.application.task.service.TaskService;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.interfaces.exception.ErrorResponse;
import me.gg.pinit.pinittask.interfaces.schedule.dto.ScheduleSimplePatchRequest;
import me.gg.pinit.pinittask.interfaces.schedule.dto.ScheduleSimpleRequest;
import me.gg.pinit.pinittask.interfaces.schedule.dto.ScheduleSimpleResponse;
import me.gg.pinit.pinittask.interfaces.utils.MemberId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v2/schedules")
@RequiredArgsConstructor
@Tag(name = "ScheduleV2", description = "일정 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "현재 상태와 충돌했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class ScheduleControllerV2 {
    private final DateTimeUtils dateTimeUtils;
    private final ScheduleService scheduleService;
    private final ScheduleStateChangeService scheduleStateChangeService;
    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "일정 생성 (작업 없이)", description = "작업과 연결하지 않는 단순 일정을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "일정이 생성되었습니다.", content = @Content(schema = @Schema(implementation = ScheduleSimpleResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증에 실패했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ScheduleSimpleResponse> createSchedule(@Parameter(hidden = true) @MemberId Long memberId,
                                                                 @Valid @RequestBody ScheduleSimpleRequest request) {
        Schedule saved = scheduleService.addSchedule(request.toSchedule(memberId, dateTimeUtils));
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleSimpleResponse.from(saved));
    }

    @GetMapping
    @Operation(summary = "일정 목록 조회 (작업 없이)", description = "지정한 날짜의 일정을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일정 목록 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleSimpleResponse.class)))),
            @ApiResponse(responseCode = "400", description = "날짜 형식이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ScheduleSimpleResponse> getSchedules(@Parameter(hidden = true) @MemberId Long memberId,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                     @RequestParam ZoneId zoneId) {
        List<Schedule> schedules = scheduleService.getScheduleList(memberId, dateTimeUtils.toZonedDateTime(time, zoneId));
        Map<Long, Task> taskMap = taskService.findTasksByIds(memberId, schedules.stream()
                        .map(Schedule::getTaskId)
                        .filter(id -> id != null)
                        .toList())
                .stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));
        return schedules.stream()
                .map(schedule -> ScheduleSimpleResponse.from(schedule, taskMap.get(schedule.getTaskId())))
                .toList();
    }

    @GetMapping("/week")
    @Operation(summary = "주간 일정 조회 (작업 없이)", description = "주어진 날짜가 포함된 주간의 일정을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주간 일정 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleSimpleResponse.class)))),
            @ApiResponse(responseCode = "400", description = "날짜 형식이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ScheduleSimpleResponse> getWeeklySchedules(@Parameter(hidden = true) @MemberId Long memberId,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                           @RequestParam ZoneId zoneId) {
        List<Schedule> schedules = scheduleService.getScheduleListForWeek(memberId, dateTimeUtils.toZonedDateTime(time, zoneId));
        Map<Long, Task> taskMap = taskService.findTasksByIds(memberId, schedules.stream()
                        .map(Schedule::getTaskId)
                        .filter(id -> id != null)
                        .toList())
                .stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));
        return schedules.stream()
                .map(schedule -> ScheduleSimpleResponse.from(schedule, taskMap.get(schedule.getTaskId())))
                .toList();
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 단건 조회 (작업 없이)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일정 단건 조회 성공", content = @Content(schema = @Schema(implementation = ScheduleSimpleResponse.class))),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ScheduleSimpleResponse getSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId) {
        Schedule schedule = scheduleService.getSchedule(memberId, scheduleId);
        Task task = null;
        if (schedule.getTaskId() != null) {
            task = taskService.getTask(memberId, schedule.getTaskId());
        }
        return ScheduleSimpleResponse.from(schedule, task);
    }

    @PatchMapping("/{scheduleId}")
    @Operation(summary = "일정 수정 (작업 없이)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일정이 수정되었습니다.", content = @Content(schema = @Schema(implementation = ScheduleSimpleResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증에 실패했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "현재 상태와 충돌했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ScheduleSimpleResponse> updateSchedule(@Parameter(hidden = true) @MemberId Long memberId,
                                                                 @PathVariable Long scheduleId,
                                                                 @RequestBody @Valid ScheduleSimplePatchRequest request) {
        Schedule updated = scheduleService.updateSchedule(memberId, scheduleId, request.toPatch(dateTimeUtils));
        return ResponseEntity.ok(ScheduleSimpleResponse.from(updated));
    }

    @PostMapping("/{scheduleId}/start")
    @Operation(summary = "일정 시작 (작업 없이)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 시작되었습니다."),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> startSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                              @RequestParam ZoneId zoneId) {
        scheduleStateChangeService.startSchedule(memberId, scheduleId, dateTimeUtils.toZonedDateTime(time, zoneId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/complete")
    @Operation(summary = "일정 완료 (작업 없이)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 완료되었습니다."),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> completeSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                 @RequestParam ZoneId zoneId) {
        scheduleStateChangeService.completeSchedule(memberId, scheduleId, dateTimeUtils.toZonedDateTime(time, zoneId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/suspend")
    @Operation(summary = "일정 일시중지 (작업 없이)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 일시중지되었습니다."),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> suspendSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                                @RequestParam ZoneId zoneId) {
        scheduleStateChangeService.suspendSchedule(memberId, scheduleId, dateTimeUtils.toZonedDateTime(time, zoneId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}/cancel")
    @Operation(summary = "일정 취소 (작업 없이)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 취소되었습니다."),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> cancelSchedule(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long scheduleId) {
        scheduleStateChangeService.cancelSchedule(memberId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제 (작업 없이)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일정이 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteSchedule(@Parameter(hidden = true) @MemberId Long memberId,
                                               @PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(memberId, scheduleId);
        return ResponseEntity.noContent().build();
    }
}
