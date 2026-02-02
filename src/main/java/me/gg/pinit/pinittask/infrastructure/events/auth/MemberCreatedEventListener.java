package me.gg.pinit.pinittask.infrastructure.events.auth;


import me.gg.pinit.pinittask.application.member.service.MemberService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class MemberCreatedEventListener {
    private final MemberService memberService;

    public MemberCreatedEventListener(MemberService memberService) {
        this.memberService = memberService;
    }

    @RabbitListener(queues = AuthMemberMessaging.MEMBER_CREATED_QUEUE)
    public void on(MemberCreatedEventDto event) {
        memberService.enrollMember(event.memberId(), event.nickname(), resolveZoneId());
    }

    private ZoneId resolveZoneId() {
        return ZoneId.of("Asia/Seoul");
    }

}
