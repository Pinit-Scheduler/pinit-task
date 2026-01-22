package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TaskCursorPageResponse(
        @Schema(description = "작업 목록")
        List<TaskResponse> data,
        @Schema(description = "다음 페이지 요청 시 사용할 커서. 더 이상 데이터가 없으면 null")
        String nextCursor,
        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {
    public static TaskCursorPageResponse of(List<TaskResponse> data, String nextCursor, boolean hasNext) {
        return new TaskCursorPageResponse(data, nextCursor, hasNext);
    }
}
