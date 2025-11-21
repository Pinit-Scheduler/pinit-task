package me.gg.pinit.pinittask.application.schedule.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class ScheduleStateChangeService {
    private final ScheduleRepository scheduleRepository;
    private final MemberService memberService;
    private final EventPublisher eventPublisher;

    @Transactional
    public void startSchedule(Long memberId, Long scheduleId, ZonedDateTime now) {
        Schedule findSchedule = scheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("해당 일정을 찾을 수 없습니다."));
        validateOwner(memberId, findSchedule);
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
        memberService.clearNowRunningSchedule(memberId);
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
}
