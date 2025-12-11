package me.gg.pinit.pinittask.application.schedule.service;

import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.events.DomainEventPublisher;
import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.domain.dependency.exception.ScheduleNotFoundException;
import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
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
        validateOwner(memberId, findSchedule);

        return findSchedule;
    }

    @Transactional(readOnly = true)
    public List<Schedule> getScheduleList(Long memberId, LocalDate date) {
        ZoneId memberZoneById = memberService.findZoneIdOfMember(memberId);
        ZonedDateTime startOfDay = date.atStartOfDay(memberZoneById);
        ZonedDateTime endExclusive = date.plusDays(1).atStartOfDay(memberZoneById);

        return scheduleRepository.findAllByOwnerIdAndDesignatedStartTimeBetween(
                memberId,
                startOfDay.toLocalDateTime(),
                endExclusive.toLocalDateTime(),
                memberZoneById.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<Schedule> getScheduleListForWeek(Long memberId, ZonedDateTime now) {
        ZonedDateTime start = dateTimeUtils.lastMondayStart(now);
        ZonedDateTime end = start.plusDays(7);
        return scheduleRepository.findAllByOwnerIdAndDesignatedStartTimeBetween(
                memberId,
                start.toLocalDateTime(),
                end.toLocalDateTime(),
                start.getZone().getId()
        );
    }

    @Transactional
    public Schedule addSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
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
        validateOwner(memberId, findSchedule);

        findSchedule.patch(updateSchedule);
        return findSchedule;
    }

    @Transactional
    public void deleteSchedule(Long memberId, Long scheduleId) {
        Schedule findSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("해당 일정을 찾을 수 없습니다."));
        validateOwner(memberId, findSchedule);

        findSchedule.deleteSchedule();
        scheduleRepository.delete(findSchedule);
        publishEvent();
    }


    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesByIds(Long memberId, List<Long> previousScheduleIds) {
        return scheduleRepository.findAllById(previousScheduleIds);
    }

    private void validateOwner(Long memberId, Schedule schedule) {
        if (!schedule.getOwnerId().equals(memberId)) {
            throw new IllegalArgumentException("Member does not own the schedule");
        }
    }

    private void publishEvent() {
        Deque<DomainEvent> queue = DomainEvents.getEventsAndClear();
        queue.forEach(domainEventPublisher::publish);
    }
}
