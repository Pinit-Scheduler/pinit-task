package me.gg.pinit.pinittask.domain.member.model;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

public class MemberUtils {
    public static Member getMemberSample(){
        return new Member(1L, "nickname", ZoneId.of("Asia/Seoul"));
    }
}
