package me.gg.pinit.pinittask.domain.schedule.model;

import jakarta.persistence.*;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.converter.service.ScheduleStateConverter;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;
import me.gg.pinit.pinittask.domain.schedule.vo.TemporalConstraint;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Schedule {
    @Id
    @Column(name = "schedule_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long ownerId;

    private String title;
    private String description;

    @Embedded
    private TemporalConstraint temporalConstraint;
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

    public Schedule(Long ownerId, String title, String description, TemporalConstraint tc, ImportanceConstraint ic) {
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.temporalConstraint = tc;
        this.importanceConstraint = ic;
    }

    public void addDependency(Schedule from) {
        Dependency dependency = new Dependency(from, this);
        dependencies.add(dependency);
    }

    public void removeDependency(Schedule from) {
        dependencies.removeIf(dependency -> dependency.getTo().equals(from));
    }

    public void start(){
        state.start(this);
    }

    public void suspend() {
        state.suspend(this);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void finish(){
        state.finish(this);
    }

    /**
     * Schedule 패키지 밖에서 사용하지 말 것
     * @param state
     */
    void setState(ScheduleState state) {
        this.state = state;
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