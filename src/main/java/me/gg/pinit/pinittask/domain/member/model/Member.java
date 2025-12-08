package me.gg.pinit.pinittask.domain.member.model;

import jakarta.persistence.*;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.converter.service.DurationConverter;
import me.gg.pinit.pinittask.domain.converter.service.LocalTimeConverter;
import me.gg.pinit.pinittask.domain.converter.service.ZoneIdConverter;
import me.gg.pinit.pinittask.domain.member.exception.DuplicatedScheduleRunningException;
import me.gg.pinit.pinittask.domain.member.exception.ObjectiveNotNullException;
import me.gg.pinit.pinittask.domain.member.exception.ObjectiveNotPositiveException;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 사용자의 목표와 루틴에 대한 정보를 기록하는 도메인
 * 시용자 정보에 위치 정보가 있음
 * 위치 정보를 이용해, 외부에서 시간 정보를 만드는 서비스가 필요하다.
 */
@Entity
public class Member {
    @Id
    @Getter
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Getter
    private String nickname;

    @Convert(converter = LocalTimeConverter.class)
    private LocalTime sleepTime;

    @Convert(converter = LocalTimeConverter.class)
    private LocalTime wakeUpTime;

    @Getter
    @Convert(converter = ZoneIdConverter.class)
    private ZoneId zoneId;

    @Getter
    @Convert(converter = DurationConverter.class)
    private Duration dailyObjectiveWork;

    @Getter
    private Long nowRunningScheduleId;

    protected Member() {
    }

    public Member(Long memberId, String nickname, ZoneId zoneId) {
        this.id = memberId;
        this.nickname = nickname;
        this.zoneId = zoneId;
        this.dailyObjectiveWork = Duration.ofHours(4);

        sleepTime = LocalTime.of(23, 0, 0, 0);
        wakeUpTime = LocalTime.of(7, 0, 0, 0);
    }

    public void setNowRunningSchedule(Long nowRunningScheduleId) {
        if (this.nowRunningScheduleId != null && !this.nowRunningScheduleId.equals(nowRunningScheduleId)) {
            throw new DuplicatedScheduleRunningException("이미 실행 중인 일정이 존재합니다.", this.nowRunningScheduleId);
        }
        if (nowRunningScheduleId == null) {
            throw new IllegalArgumentException("실행 중인 일정 ID는 비어있을 수 없습니다.");
        }
        this.nowRunningScheduleId = nowRunningScheduleId;
    }

    public void clearNowRunningSchedule() {
        this.nowRunningScheduleId = null;
    }

    public void setDailyObjectiveWork(Duration dailyObjectiveWork) {
        validateDuration(dailyObjectiveWork);
        this.dailyObjectiveWork = dailyObjectiveWork;
    }

    private void validateDuration(Duration dailyObjectiveWork) {
        if (dailyObjectiveWork == null) {
            throw new ObjectiveNotNullException("일일 목표 작업 시간이 비어 있을 수 없습니다.");
        }
        if(dailyObjectiveWork.isNegative() || dailyObjectiveWork.isZero()) {
            throw new ObjectiveNotPositiveException("일일 목표 작업 시간은 0분을 넘어야 합니다.");
        }
    }
}
