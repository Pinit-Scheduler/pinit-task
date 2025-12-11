package me.gg.pinit.pinittask.application.statistics.event.handler;

import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.statistics.service.StatisticsService;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCanceledEvent;
import me.gg.pinit.pinittask.domain.schedule.model.CompletedState;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ScheduleCanceledEventHandler {
    private final StatisticsService statisticsService;
    private final ScheduleService scheduleService;

    public ScheduleCanceledEventHandler(StatisticsService statisticsService, ScheduleService scheduleService) {
        this.statisticsService = statisticsService;
        this.scheduleService = scheduleService;
    }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ScheduleCanceledEvent event) {
        if (!event.getBeforeState().equals(CompletedState.COMPLETED)) return;
        Schedule schedule = scheduleService.getSchedule(event.getOwnerId(), event.getScheduleId());
        statisticsService.removeElapsedTime(
                event.getOwnerId(),
                schedule.getTemporalConstraint().getTaskType(),
                schedule.getHistory().getElapsedTime(),
                schedule.getHistory().getStartTime()
        );
    }
}


/**
 * cancel이 현재 기록된 시간대로 삭제함
 * 따라서 진행하다 취소해도 기록된 시간만큼 삭제함
 * 현재 "완료 상태에서 cancel" 이 "cancel 전체"를 사용하고 있음
 * (원래라면 schedueState 내에서 따로 해야할 연산이었음. 외부 애그리거트와의 통신때문에 도메인 서비스로 쓰이는 중)
 * <p>
 * 2가지 해법
 * - 기존 상태의 스냅샷 저장
 * - 서비스 호출로 변경
 */