package me.gg.pinit.pinittask.domain.member.model;

import java.time.Clock;
import java.time.Duration;

public class MemberUtils {
    public static Member getMemberSample(){
        return new Member("testMember", "testNickname", Duration.ofHours(4), Clock.systemDefaultZone().getZone());
    }
}
