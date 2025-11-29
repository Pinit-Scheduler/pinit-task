package me.gg.pinit.pinittask.interfaces.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.interfaces.utils.MemberId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/now")
    @Operation(summary = "현재 진행 중인 일정 ID 조회", description = "사용자의 현재 진행 중인 일정 ID를 조회합니다.")
    public ResponseEntity<Long> getNowInProgressScheduleId(@MemberId Long memberId) {
        Long scheduleId = memberService.getNowInProgressScheduleId(memberId);
        return ResponseEntity.ok(scheduleId);
    }
}
