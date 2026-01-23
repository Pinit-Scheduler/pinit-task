package me.gg.pinit.pinittask.infrastructure.schedule;

import com.querydsl.jpa.impl.JPAQueryFactory;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.repository.ScheduleRepositoryCustom;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static me.gg.pinit.pinittask.domain.schedule.model.QSchedule.schedule;

@Repository
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    public ScheduleRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Schedule> findAllByOwnerIdAndDesignatedStartTimeBetween(Long ownerId, Instant start, Instant end) {
        return jpaQueryFactory.select(schedule)
                .from(schedule)
                .where(
                        schedule.ownerId.eq(ownerId)
                                .and(schedule.designatedStartTime.goe(start))
                                .and(schedule.designatedStartTime.lt(end)))
                .fetch();
    }
}
