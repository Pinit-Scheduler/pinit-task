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
import me.gg.pinit.pinittask.application.task.service.TaskAdjustmentService;
import me.gg.pinit.pinittask.application.task.service.TaskService;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.interfaces.dto.ScheduleSimpleResponse;
import me.gg.pinit.pinittask.interfaces.dto.TaskRequest;
import me.gg.pinit.pinittask.interfaces.dto.TaskResponse;
import me.gg.pinit.pinittask.interfaces.dto.TaskScheduleRequest;
import me.gg.pinit.pinittask.interfaces.exception.ErrorResponse;
import me.gg.pinit.pinittask.interfaces.utils.MemberId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task", description = "작업 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "현재 상태와 충돌했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class TaskControllerV1 {
    private final DateTimeUtils dateTimeUtils;
    private final TaskAdjustmentService taskAdjustmentService;
    private final TaskService taskService;
    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "작업 생성", description = "새 작업과 의존 관계를 등록합니다.")
    public ResponseEntity<TaskResponse> createTask(@Parameter(hidden = true) @MemberId Long memberId,
                                                   @Valid @RequestBody TaskRequest request) {
        Task saved = taskAdjustmentService.createTask(memberId, request.toCommand(null, memberId, dateTimeUtils));
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(saved));
    }

    @PatchMapping("/{taskId}")
    @Operation(summary = "작업 수정", description = "작업 본문과 의존 관계를 함께 수정합니다.")
    public ResponseEntity<TaskResponse> updateTask(@Parameter(hidden = true) @MemberId Long memberId,
                                                   @PathVariable Long taskId,
                                                   @Valid @RequestBody TaskRequest request) {
        Task updated = taskAdjustmentService.updateTask(memberId, request.toCommand(taskId, memberId, dateTimeUtils));
        return ResponseEntity.ok(TaskResponse.from(updated));
    }

    @GetMapping
    @Operation(summary = "작업 목록 조회", description = "회원의 작업 목록을 조회합니다.")
    public List<TaskResponse> getTasks(@Parameter(hidden = true) @MemberId Long memberId) {
        return taskService.getTasks(memberId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "작업 단건 조회", description = "특정 작업의 상세 정보를 조회합니다.")
    public TaskResponse getTask(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long taskId) {
        Task task = taskService.getTask(memberId, taskId);
        return TaskResponse.from(task);
    }

    @PostMapping("/{taskId}/complete")
    @Operation(summary = "작업 완료", description = "작업을 완료 상태로 변경합니다.")
    public ResponseEntity<Void> completeTask(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long taskId) {
        taskService.markCompleted(memberId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/reopen")
    @Operation(summary = "작업 되돌리기", description = "작업을 미완료 상태로 되돌립니다.")
    public ResponseEntity<Void> reopenTask(@Parameter(hidden = true) @MemberId Long memberId, @PathVariable Long taskId) {
        taskService.markIncomplete(memberId, taskId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "작업 삭제", description = "작업과 그 작업에 관련된 의존 관계를 삭제합니다.")
    public ResponseEntity<Void> deleteTask(@Parameter(hidden = true) @MemberId Long memberId,
                                           @PathVariable Long taskId,
                                           @RequestParam(defaultValue = "false") boolean deleteSchedules) {
        taskService.deleteTask(memberId, taskId, deleteSchedules);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/schedules")
    @Operation(summary = "작업을 일정으로 등록", description = "기존 작업을 지정한 시간의 일정으로 복사합니다.")
    public ResponseEntity<ScheduleSimpleResponse> createScheduleFromTask(@Parameter(hidden = true) @MemberId Long memberId,
                                                                         @PathVariable Long taskId,
                                                                         @Valid @RequestBody TaskScheduleRequest request) {
        Task task = taskService.getTask(memberId, taskId);
        Schedule saved = scheduleService.addSchedule(request.toSchedule(task, memberId, dateTimeUtils));
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleSimpleResponse.from(saved));
    }
}
