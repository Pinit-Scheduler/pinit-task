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
        // TODO 이벤트 페이로드에 timezone 정보를 추가해야 하나? 아니면 gRPC로 MemberService에 조회를 하나?
        // 어쩌면 이벤트가 아닌 프론트에서 직접 등록 API를 호출하게 하는 것이 맞을수도 (일단 킵
        return ZoneId.of("Asia/Seoul");
    }

}
