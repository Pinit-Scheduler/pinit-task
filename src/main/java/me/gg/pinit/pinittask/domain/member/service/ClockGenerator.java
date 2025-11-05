package me.gg.pinit.pinittask.domain.member.service;

import me.gg.pinit.pinittask.domain.member.model.Member;

import java.time.Clock;
import java.time.ZoneId;

public class ClockGenerator {
    public Clock generateClock(ZoneId zoneId) {
        return Clock.system(zoneId);
    }
}
