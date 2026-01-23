package me.gg.pinit.pinittask.interfaces.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.interfaces.exception.ErrorResponse;
import me.gg.pinit.pinittask.interfaces.utils.MemberId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@RestController
@RequestMapping("/v1/members")
@Tag(name = "MemberV1", description = "회원 관련 정보 API (v1)")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "대상을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@RequiredArgsConstructor
public class MemberControllerV1 {

    private final MemberService memberService;

    @GetMapping("/now")
    @Operation(summary = "현재 진행 중인 일정 ID 조회", description = "사용자의 현재 진행 중인 일정 ID를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "현재 진행 중인 일정 ID 조회 성공", content = @Content(schema = @Schema(implementation = Long.class, nullable = true)))
    })
    public ResponseEntity<Long> getNowInProgressScheduleId(@Parameter(hidden = true) @MemberId Long memberId) {
        Long scheduleId = memberService.getNowInProgressScheduleId(memberId);
        return ResponseEntity.ok(scheduleId);
    }

    @GetMapping("/zone-offset")
    @Operation(summary = "사용자 시간대 조회", description = "사용자의 시간대를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 시간대 조회 성공", content = @Content(schema = @Schema(implementation = String.class, example = "+09:00"))),
    })
    public ResponseEntity<String> getMemberZoneOffset(@Parameter(hidden = true) @MemberId Long memberId) {
        String zoneOffset = memberService.findZoneOffsetOfMember(memberId).toString();
        return ResponseEntity.ok(zoneOffset);
    }
}
