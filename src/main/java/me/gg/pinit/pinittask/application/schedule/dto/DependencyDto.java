package me.gg.pinit.pinittask.application.schedule.dto;

import lombok.Getter;

@Getter
public class DependencyDto {
    private Long id;
    private Long fromId;
    private Long toId;

    public DependencyDto(Long id, Long fromId, Long toId) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
    }
}
