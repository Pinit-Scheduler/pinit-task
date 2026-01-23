package me.gg.pinit.pinittask.interfaces.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TaskCursorPageResponseV2(
        @Schema(description = "작업 목록")
        List<TaskResponseV2> data,
        @Schema(description = "다음 페이지 요청 시 사용할 커서. 더 이상 데이터가 없으면 null")
        String nextCursor,
        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {
    public static TaskCursorPageResponseV2 of(List<TaskResponseV2> data, String nextCursor, boolean hasNext) {
        return new TaskCursorPageResponseV2(data, nextCursor, hasNext);
    }
}
