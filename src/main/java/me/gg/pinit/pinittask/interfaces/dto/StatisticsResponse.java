package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.statistics.model.Statistics;

import java.time.Duration;

@Getter
@Schema(description = "사용자 주간 통계 응답 DTO")
public class StatisticsResponse {
    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "통계 기준 주간의 시작 시각")
    private DateTimeWithZone startOfWeek;

    @Schema(description = "딥 워크 누적 시간")
    private Duration deepWorkElapsedTime;
    @Schema(description = "관리 업무 누적 시간")
    private Duration adminWorkElapsedTime;
    @Schema(description = "총 작업 누적 시간")
    private Duration totalWorkElapsedTime;

    public static StatisticsResponse from(Statistics statistics) {
        StatisticsResponse response = new StatisticsResponse();
        response.memberId = statistics.getMemberId();
        response.startOfWeek = DateTimeWithZone.from(statistics.getStartOfWeek());
        response.deepWorkElapsedTime = statistics.getDeepWorkElapsedTime();
        response.adminWorkElapsedTime = statistics.getAdminWorkElapsedTime();
        response.totalWorkElapsedTime = statistics.getTotalWorkElapsedTime();
        return response;
    }
}
