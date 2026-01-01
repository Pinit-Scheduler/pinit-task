package me.gg.pinit.pinittask.application.schedule.service;

import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.events.DomainEventPublisher;
import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.domain.dependency.exception.ScheduleNotFoundException;
import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleTimeUpdatedEvent;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Deque;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final MemberService memberService;
    private final DomainEventPublisher domainEventPublisher;
    private final DateTimeUtils dateTimeUtils;


    @Transactional(readOnly = true)
    public Schedule getSchedule(Long memberId, Long scheduleId) {
        Schedule findSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("해당 일정을 찾을 수 없습니다."));

        return findSchedule;
    }

    @Transactional(readOnly = true)
    public List<Schedule> getScheduleList(Long memberId, ZonedDateTime dateTime) {
        ZoneId memberZoneById = memberService.findZoneIdOfMember(memberId);
        Instant startOfDay = dateTime.toLocalDate().atStartOfDay(memberZoneById).toInstant();
        Instant endExclusive = dateTime.toLocalDate().plusDays(1).atStartOfDay(memberZoneById).toInstant();

        return scheduleRepository.findAllByOwnerIdAndDesignatedStartTimeInstantBetween(
                memberId,
                startOfDay,
                endExclusive
        );
    }

    @Transactional(readOnly = true)
    public List<Schedule> getScheduleListForWeek(Long memberId, ZonedDateTime now) {
        ZoneOffset zoneOffsetOfMember = memberService.findZoneOffsetOfMember(memberId);
        Instant start = dateTimeUtils.lastMondayStart(now, zoneOffsetOfMember).toInstant();
        Instant end = dateTimeUtils.lastMondayStart(now, zoneOffsetOfMember).plusDays(7).toInstant();
        return scheduleRepository.findAllByOwnerIdAndDesignatedStartTimeInstantBetween(
                memberId,
                start,
                end
        );
    }

    @Transactional
    public Schedule addSchedule(Schedule schedule) {
        Schedule saved = scheduleRepository.save(schedule);
        domainEventPublisher.publish(new ScheduleTimeUpdatedEvent(saved.getId(), saved.getOwnerId(), saved.getDesignatedStartTime()));
        return saved;
    }

    /**
     * 일정 수정
     * 프론트에 편하게 만들 경우, 백엔드에서 뭐가 변경되었는지 명확히 알지 못하는 문제가 존재한다.
     * 그렇다고 엔티티를 바로 merge 하게 되면, DTO에 없는 필드가 null로 복사되어 기존 값이 소거될 수 있다.
     * 따라서 프론트에서 보내줄 때 변경된 부분을 리스트로 함께 전달하고, 그 부분만을 선택적으로 업데이트 하는 방식을 사용한다.
     *
     * @param memberId
     * @param updateSchedule
     * @return
     */
    @Transactional
    public Schedule updateSchedule(Long memberId, Long scheduleId, SchedulePatch updateSchedule) {
        Schedule findSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("해당 일정을 찾을 수 없습니다."));

        findSchedule.patch(updateSchedule);
        publishEvent();
        return findSchedule;
    }

    @Transactional
    public void deleteSchedule(Long memberId, Long scheduleId) {
        Schedule findSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("해당 일정을 찾을 수 없습니다."));

        findSchedule.deleteSchedule();
        scheduleRepository.delete(findSchedule);
        publishEvent();
    }


    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesByIds(Long memberId, List<Long> previousScheduleIds) {
        return scheduleRepository.findAllById(previousScheduleIds);
    }


    private void publishEvent() {
        Deque<DomainEvent> queue = DomainEvents.getEventsAndClear();
        queue.forEach(domainEventPublisher::publish);
    }
}
