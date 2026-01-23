package me.gg.pinit.pinittask.interfaces.statistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.statistics.service.StatisticsService;
import me.gg.pinit.pinittask.interfaces.exception.ErrorResponse;
import me.gg.pinit.pinittask.interfaces.statistics.dto.StatisticsResponse;
import me.gg.pinit.pinittask.interfaces.utils.MemberId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/v1/statistics")
@Tag(name = "StatisticsV1", description = "통계 조회 API (v1)")
@RequiredArgsConstructor
public class StatisticsControllerV1 {

    private final StatisticsService statisticsService;
    private final DateTimeUtils dateTimeUtils;

    @GetMapping
    @Operation(summary = "사용자 통계 조회", description = "주어진 시점 기준 주간 통계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "통계를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public StatisticsResponse getStatistics(
            @Parameter(hidden = true) @MemberId Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
            @RequestParam ZoneId zoneId
    ) {
        return StatisticsResponse.from(statisticsService.getStatistics(memberId, dateTimeUtils.toZonedDateTime(time, zoneId)));
    }
}
