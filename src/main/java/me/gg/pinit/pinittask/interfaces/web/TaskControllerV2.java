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
import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.task.service.TaskAdjustmentService;
import me.gg.pinit.pinittask.application.task.service.TaskService;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.interfaces.dto.*;
import me.gg.pinit.pinittask.interfaces.exception.ErrorResponse;
import me.gg.pinit.pinittask.interfaces.utils.MemberId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/tasks")
@RequiredArgsConstructor
@Tag(name = "TaskV2", description = "작업 관리 API (마감 날짜 + 오프셋 기반)")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "현재 상태와 충돌했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class TaskControllerV2 {
    private final DateTimeUtils dateTimeUtils;
    private final DependencyService dependencyService;
    private final TaskAdjustmentService taskAdjustmentService;
    private final TaskService taskService;
    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "작업 생성", description = "새 작업과 의존 관계를 등록합니다. 마감은 날짜 + UTC 오프셋(시간 00:00 고정)으로 입력합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작업이 생성되었습니다.", content = @Content(schema = @Schema(implementation = TaskResponseV2.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증에 실패했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "현재 상태와 충돌했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponseV2> createTask(@Parameter(hidden = true) @MemberId Long memberId,
                                                     @Valid @RequestBody TaskCreateRequestV2 request) {
        Task saved = taskAdjustmentService.createTask(memberId, request.toCommand(null, memberId, dateTimeUtils));
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponseV2.from(saved));
    }

    @PatchMapping("/{taskId}")
    @Operation(summary = "작업 수정", description = "작업 본문과 의존 관계를 함께 수정합니다. 마감 날짜는 00:00:00 기준으로 저장됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업이 수정되었습니다.", content = @Content(schema = @Schema(implementation = TaskResponseV2.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증에 실패했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "작업을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "현재 상태와 충돌했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponseV2> updateTask(@Parameter(hidden = true) @MemberId Long memberId,
                                                     @PathVariable Long taskId,
                                                     @Valid @RequestBody TaskUpdateRequestV2 request) {
        Task updated = taskAdjustmentService.updateTask(memberId, request.toCommand(taskId, memberId, dateTimeUtils));
        return ResponseEntity.ok(TaskResponseV2.from(updated));
    }

    @GetMapping
    @Operation(summary = "작업 목록 조회", description = "회원의 작업 목록을 조회합니다. page/size로 마감 날짜 오름차순 페이지네이션, readyOnly로 선행 작업 없는 항목만 필터링합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 목록 조회 성공")
    })
    public Page<TaskResponseV2> getTasks(@Parameter(hidden = true) @MemberId Long memberId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(defaultValue = "false") boolean readyOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("temporalConstraint.deadline.date")));
        Page<Task> tasks = taskService.getTasks(memberId, pageable, readyOnly);
        var dependencyMap = dependencyService.getDependencyInfoForTasks(memberId, tasks.getContent().stream().map(Task::getId).toList());
        return tasks.map(task -> TaskResponseV2.from(task, dependencyMap.get(task.getId())));
    }

    @GetMapping("/cursor")
    @Operation(summary = "작업 목록 커서 조회", description = "마감 날짜(00:00:00) asc, id asc 커서 기반 페이지네이션. cursor는 'YYYY-MM-DDTHH:MM:SS|taskId' 형식(시간은 항상 00:00:00)입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커서 기반 작업 목록 조회 성공", content = @Content(schema = @Schema(implementation = TaskCursorPageResponseV2.class))),
            @ApiResponse(responseCode = "400", description = "커서 형식이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TaskCursorPageResponseV2 getTasksByCursor(@Parameter(hidden = true) @MemberId Long memberId,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(required = false) String cursor,
                                                     @RequestParam(defaultValue = "false") boolean readyOnly) {
        TaskService.CursorPage page = taskService.getTasksByCursorPage(memberId, size, cursor, readyOnly);
        var dependencyInfoMap = dependencyService.getDependencyInfoForTasks(memberId, page.tasks().stream().map(Task::getId).toList());
        List<TaskResponseV2> data = page.tasks().stream()
                .map(task -> TaskResponseV2.from(task, dependencyInfoMap.get(task.getId())))
                .toList();
        return TaskCursorPageResponseV2.of(data, page.nextCursor(), page.hasNext());
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "작업 단건 조회", description = "특정 작업의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 단건 조회 성공", content = @Content(schema = @Schema(implementation = TaskResponseV2.class))),
            @ApiResponse(responseCode = "404", description = "작업을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TaskResponseV2 getTask(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long taskId) {
        Task task = taskService.getTask(memberId, taskId);
        var dependencyInfo = dependencyService.getDependencyInfo(memberId, taskId);
        return TaskResponseV2.from(task, dependencyInfo);
    }

    @PostMapping("/{taskId}/complete")
    @Operation(summary = "작업 완료", description = "작업을 완료 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "작업이 완료되었습니다."),
            @ApiResponse(responseCode = "404", description = "작업을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> completeTask(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long taskId) {
        taskService.markCompleted(memberId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/reopen")
    @Operation(summary = "작업 되돌리기", description = "작업을 미완료 상태로 되돌립니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "작업이 되돌려졌습니다."),
            @ApiResponse(responseCode = "404", description = "작업을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전환입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> reopenTask(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long taskId) {
        taskService.markIncomplete(memberId, taskId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "작업 삭제", description = "작업과 그 작업에 관련된 의존 관계를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "작업이 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "작업을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteTask(@Parameter(hidden = true) @MemberId Long memberId,
                                           @PathVariable Long taskId,
                                           @RequestParam(defaultValue = "false") boolean deleteSchedules) {
        taskService.deleteTask(memberId, taskId, deleteSchedules);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/schedules")
    @Operation(summary = "작업을 일정으로 등록", description = "기존 작업을 지정한 시간의 일정으로 복사합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작업이 일정으로 등록되었습니다.", content = @Content(schema = @Schema(implementation = ScheduleSimpleResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증에 실패했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "작업을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ScheduleSimpleResponse> createScheduleFromTask(@Parameter(hidden = true) @MemberId Long memberId,
                                                                         @PathVariable Long taskId,
                                                                         @Valid @RequestBody TaskScheduleRequest request) {
        Task task = taskService.getTask(memberId, taskId);
        Schedule saved = scheduleService.addSchedule(request.toSchedule(task, memberId, dateTimeUtils));
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleSimpleResponse.from(saved));
    }
}
