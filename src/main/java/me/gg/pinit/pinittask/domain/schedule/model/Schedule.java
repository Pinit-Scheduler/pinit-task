package me.gg.pinit.pinittask.domain.schedule.model;

import jakarta.persistence.*;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.converter.service.ScheduleStateConverter;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalDescriptionException;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTitleException;
import me.gg.pinit.pinittask.domain.schedule.exception.TimeOrderReversedException;
import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;
import me.gg.pinit.pinittask.domain.schedule.vo.TemporalConstraint;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Schedule {
    @Id
    @Column(name = "schedule_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Getter
    private Long ownerId;

    @Getter
    private String title;
    @Getter
    private String description;

    @Getter
    private ZonedDateTime date;

    @Getter
    @Embedded
    private TemporalConstraint temporalConstraint;
    @Getter
    @Embedded
    private ImportanceConstraint importanceConstraint;
    @Getter
    @Embedded
    private ScheduleHistory history = ScheduleHistory.zero();

    @Convert(converter = ScheduleStateConverter.class)
    private ScheduleState state;

    @OneToMany(mappedBy = "to")
    private final Set<Dependency> dependencies = new HashSet<>();

    protected Schedule() {}

    public Schedule(Long ownerId, String title, String description, ZonedDateTime zdt, TemporalConstraint tc, ImportanceConstraint ic) {
        this.ownerId = ownerId;
        setTitle(title);
        setDescription(description);
        this.temporalConstraint = tc;
        this.importanceConstraint = ic;
        setDate(zdt);
    }

    public void setTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    public void setDescription(String description) {
        validateDescrption(description);
        this.description = description;
    }

    public void setDate(ZonedDateTime zdt) {
        validateDate(zdt);
        this.date = zdt;
    }

    public void addDependency(Schedule from) {
        Dependency dependency = new Dependency(from, this);
        dependencies.add(dependency);
    }

    public void removeDependency(Schedule from) {
        dependencies.removeIf(dependency -> dependency.getTo().equals(from));
    }

    public void start(ZonedDateTime startTime) {
        state.start(this, startTime);
    }

    public void suspend(ZonedDateTime suspendTime) {
        state.suspend(this, suspendTime);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void finish(ZonedDateTime finishTime) {
        state.finish(this, finishTime);
    }

    /**
     * Schedule 패키지 밖에서 사용하지 말 것
     * @param state
     */
    void setState(ScheduleState state) {
        this.state = state;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalTitleException("제목의 길이는 1자 이상 20자 이하여야 합니다.");
        } else if (title.length() > 20) {
            throw new IllegalTitleException("제목의 길이는 1자 이상 20자 이하여야 합니다.");
        }
    }

    private void validateDescrption(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalDescriptionException("설명의 길이는 1자 이상 100자 이하여야 합니다.");
        }else if (description.length() > 100) {
            throw new IllegalDescriptionException("설명의 길이는 1자 이상 100자 이하여야 합니다.");
        }
    }

    private void validateDate(ZonedDateTime zdt) {
        if(zdt.isAfter(this.temporalConstraint.getDeadline())) {
            throw new TimeOrderReversedException("일정의 날짜는 데드라인을 초과할 수 없습니다.");
        }
    }
}

/**
 * 현재 진행중인 일정이 있는지 확인은 어떤 방식으로 해야 하지?
 * 이러면 일정 -> 멤버 방향으로 의존관계가 필요해진다.
 * 모든 일정은 해당 일정 생성자의 통계를 알고 있도록? 너무 많지 않나?
 * 일정 도메인
 * - 목표 일일 작업 시간
 * - 현재 진행하고 있는 일정 << 얘 혼자만 좀 위치가 붕 뜬다. 통계도 아니다 이건
 * 결국 일정이 회원을 알아야 하는 것은 변함이 없음
 * 피할 수 없는 것 -> 알게 할 필요 있음
 * 직접 아는 것이 아니라, 해당 서비스에게 위임하게 만들 수 있나?
 */