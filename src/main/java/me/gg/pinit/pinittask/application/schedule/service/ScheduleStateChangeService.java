package me.gg.pinit.pinittask.application.schedule.service;

import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.events.EventPublisher;
import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.domain.dependency.exception.ScheduleNotFoundException;
import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Deque;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleStateChangeService {
    private final ScheduleRepository scheduleRepository;
    private final MemberService memberService;
    private final EventPublisher eventPublisher;
    private final DependencyService dependencyService;
    private final ScheduleService scheduleService;

    @Transactional
    public void startSchedule(Long memberId, Long scheduleId, ZonedDateTime now) {
        Schedule findSchedule = scheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("해당 일정을 찾을 수 없습니다."));
        validateOwner(memberId, findSchedule);
        checkBeforeScheduleIsCompleted(memberId, scheduleId);

        memberService.setNowRunningSchedule(memberId, scheduleId);

        findSchedule.start(now);
        publishEvent();
    }

    @Transactional
    public void completeSchedule(Long memberId, Long scheduleId, ZonedDateTime now) {
        Schedule findSchedule = scheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("해당 일정을 찾을 수 없습니다."));
        validateOwner(memberId, findSchedule);
        memberService.clearNowRunningSchedule(memberId);
        findSchedule.finish(now);
        publishEvent();
    }

    @Transactional
    public void suspendSchedule(Long memberId, Long scheduleId, ZonedDateTime now) {
        Schedule findSchedule = scheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("해당 일정을 찾을 수 없습니다."));
        validateOwner(memberId, findSchedule);
        findSchedule.suspend(now);
        publishEvent();
    }

    @Transactional
    public void cancelSchedule(Long memberId, Long scheduleId) {
        Schedule findSchedule = scheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("해당 일정을 찾을 수 없습니다."));
        validateOwner(memberId, findSchedule);
        if (findSchedule.isInProgress() || findSchedule.isSuspended()) memberService.clearNowRunningSchedule(memberId);
        findSchedule.cancel();
        publishEvent();
    }

    private void validateOwner(Long memberId, Schedule schedule) {
        if (!schedule.getOwnerId().equals(memberId)) {
            throw new IllegalArgumentException("Member does not own the schedule");
        }
    }

    private void publishEvent() {
        Deque<DomainEvent> queue = DomainEvents.getEventsAndClear();
        queue.forEach(eventPublisher::publish);
    }

    private void checkBeforeScheduleIsCompleted(Long memberId, Long scheduleId) {
        List<Long> previousScheduleIds = dependencyService.getPreviousScheduleIds(memberId, scheduleId);
        scheduleService.findSchedulesByIds(memberId, previousScheduleIds).forEach(schedule -> {
            if (!schedule.isCompleted()) {
                throw new IllegalStateException("이전 일정이 완료되지 않아 해당 일정을 시작할 수 없습니다.");
            }
        });
    }
}
