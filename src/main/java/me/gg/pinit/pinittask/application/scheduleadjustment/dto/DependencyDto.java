package me.gg.pinit.pinittask.application.scheduleadjustment.dto;

import lombok.Getter;

@Getter
public class DependencyDto {
    private Long fromId;
    private Long toId;

    public DependencyDto(Long fromId, Long toId) {
        this.fromId = fromId;
        this.toId = toId;
    }
}
