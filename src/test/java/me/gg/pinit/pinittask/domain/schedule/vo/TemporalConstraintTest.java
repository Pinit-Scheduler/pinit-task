package me.gg.pinit.pinittask.domain.schedule.vo;

import me.gg.pinit.pinittask.domain.schedule.model.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemporalConstraintTest {
    @Mock
    Clock clock;

    @BeforeEach
    void setup(){
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(Instant.parse("2025-01-01T10:00:00Z"));
    }

    @Test
    void Equals_서로_같은_경우(){
        //when
        TemporalConstraint tc = new TemporalConstraint(ZonedDateTime.now(clock), Duration.ofHours(3), TaskType.DEEP_WORK);
        TemporalConstraint sameTc = new TemporalConstraint(ZonedDateTime.now(clock), Duration.ofHours(3), TaskType.DEEP_WORK);

        //then
        assertEquals(tc, sameTc, "TemporalConstraint 객체가 동일한 값으로 생성되었을 때 equals 메서드는 true를 반환해야 합니다.");

    }

    @Test
    void Equals_Duration_불일치(){
        //when
        TemporalConstraint tc = new TemporalConstraint(ZonedDateTime.now(clock), Duration.ofHours(3), TaskType.DEEP_WORK);
        TemporalConstraint diffTc = new TemporalConstraint(ZonedDateTime.now(clock), Duration.ofHours(4), TaskType.DEEP_WORK);

        //then
        assertNotEquals(tc, diffTc, "TemporalConstraint 객체가 Duration 값이 다를 때 equals 메서드는 false를 반환해야 합니다.");
    }

    @Test
    void Equals_시각_불일치(){
        //when
        TemporalConstraint tc = new TemporalConstraint(ZonedDateTime.now(clock), Duration.ofHours(3), TaskType.DEEP_WORK);
        TemporalConstraint diffTc = new TemporalConstraint(ZonedDateTime.now(clock).plusHours(3), Duration.ofHours(3), TaskType.DEEP_WORK);

        //then
        assertNotEquals(tc, diffTc, "TemporalConstraint 객체가 deadline 값이 다를 때 equals 메서드는 false를 반환해야 합니다.");
    }

    @Test
    void Equals_일정_타입_불일치(){
        //when
        TemporalConstraint tc = new TemporalConstraint(ZonedDateTime.now(clock), Duration.ofHours(3), TaskType.DEEP_WORK);
        TemporalConstraint diffTc = new TemporalConstraint(ZonedDateTime.now(clock), Duration.ofHours(3), TaskType.ADMIN_TASK);

        //then
        assertNotEquals(tc, diffTc, "TemporalConstraint 객체가 allocateType 값이 다를 때 equals 메서드는 false를 반환해야 합니다.");
    }
}