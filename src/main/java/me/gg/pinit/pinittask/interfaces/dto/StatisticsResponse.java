package me.gg.pinit.pinittask.interfaces.dto;

import lombok.Getter;
import me.gg.pinit.pinittask.domain.statistics.model.Statistics;

import java.time.Duration;
import java.time.ZonedDateTime;

@Getter
public class StatisticsResponse {
    private Long memberId;

    private ZonedDateTime startOfWeek;

    private Duration deepWorkElapsedTime;
    private Duration adminWorkElapsedTime;
    private Duration totalWorkElapsedTime;

    public static StatisticsResponse from(Statistics statistics) {
        StatisticsResponse response = new StatisticsResponse();
        response.memberId = statistics.getMemberId();
        response.startOfWeek = statistics.getStartOfWeek();
        response.deepWorkElapsedTime = statistics.getDeepWorkElapsedTime();
        response.adminWorkElapsedTime = statistics.getAdminWorkElapsedTime();
        response.totalWorkElapsedTime = statistics.getTotalWorkElapsedTime();
        return response;
    }
}
