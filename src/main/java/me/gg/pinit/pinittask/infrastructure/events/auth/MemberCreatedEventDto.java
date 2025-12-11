package me.gg.pinit.pinittask.infrastructure.events.auth;

import java.time.LocalDateTime;

public record MemberCreatedEventDto(Long memberId, String nickname, LocalDateTime occurredAt) {
}
