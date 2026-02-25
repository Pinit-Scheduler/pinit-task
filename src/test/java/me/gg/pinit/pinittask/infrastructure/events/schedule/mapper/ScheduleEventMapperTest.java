package me.gg.pinit.pinittask.infrastructure.events.schedule.mapper;

import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCanceledEvent;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleStartedEvent;
import me.gg.pinit.pinittask.infrastructure.events.schedule.ScheduleMessaging;
import me.gg.pinit.pinittask.infrastructure.events.schedule.dto.ScheduleCanceledPayload;
import me.gg.pinit.pinittask.infrastructure.events.schedule.dto.ScheduleCompletedPayload;
import me.gg.pinit.pinittask.infrastructure.events.schedule.dto.ScheduleStartedPayload;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleEventMapperTest {

    @Test
    void startedMapper_mapsToExpectedExchangeRoutingAndPayload() {
        ScheduleStartedEventMapper mapper = new ScheduleStartedEventMapper();
        ScheduleStartedEvent event = new ScheduleStartedEvent(11L, 101L, "SUSPENDED");

        Object mapped = mapper.payload(event);

        assertThat(mapper.exchange()).isEqualTo(ScheduleMessaging.DIRECT_EXCHANGE);
        assertThat(mapper.routingKey()).isEqualTo(ScheduleMessaging.RK_SCHEDULE_STARTED);
        assertThat(mapped).isInstanceOf(ScheduleStartedPayload.class);

        ScheduleStartedPayload payload = (ScheduleStartedPayload) mapped;
        assertThat(payload.scheduleId()).isEqualTo(11L);
        assertThat(payload.ownerId()).isEqualTo(101L);
        assertThat(payload.beforeState()).isEqualTo("SUSPENDED");
        assertThat(payload.idempotentKey()).isNotBlank();
        assertThat(OffsetDateTime.parse(payload.occurredAt())).isNotNull();
    }

    @Test
    void completedMapper_mapsToExpectedExchangeRoutingAndPayload() {
        ScheduleCompletedEventMapper mapper = new ScheduleCompletedEventMapper();
        ScheduleCompletedEvent event = new ScheduleCompletedEvent(12L, 102L, "IN_PROGRESS");

        Object mapped = mapper.payload(event);

        assertThat(mapper.exchange()).isEqualTo(ScheduleMessaging.DIRECT_EXCHANGE);
        assertThat(mapper.routingKey()).isEqualTo(ScheduleMessaging.RK_SCHEDULE_COMPLETED);
        assertThat(mapped).isInstanceOf(ScheduleCompletedPayload.class);

        ScheduleCompletedPayload payload = (ScheduleCompletedPayload) mapped;
        assertThat(payload.scheduleId()).isEqualTo(12L);
        assertThat(payload.ownerId()).isEqualTo(102L);
        assertThat(payload.beforeState()).isEqualTo("IN_PROGRESS");
        assertThat(payload.idempotentKey()).isNotBlank();
        assertThat(OffsetDateTime.parse(payload.occurredAt())).isNotNull();
    }

    @Test
    void canceledMapper_mapsToExpectedExchangeRoutingAndPayload() {
        ScheduleCanceledEventMapper mapper = new ScheduleCanceledEventMapper();
        ScheduleCanceledEvent event = new ScheduleCanceledEvent(13L, 103L, "COMPLETED");

        Object mapped = mapper.payload(event);

        assertThat(mapper.exchange()).isEqualTo(ScheduleMessaging.DIRECT_EXCHANGE);
        assertThat(mapper.routingKey()).isEqualTo(ScheduleMessaging.RK_SCHEDULE_CANCELED);
        assertThat(mapped).isInstanceOf(ScheduleCanceledPayload.class);

        ScheduleCanceledPayload payload = (ScheduleCanceledPayload) mapped;
        assertThat(payload.scheduleId()).isEqualTo(13L);
        assertThat(payload.ownerId()).isEqualTo(103L);
        assertThat(payload.beforeState()).isEqualTo("COMPLETED");
        assertThat(payload.idempotentKey()).isNotBlank();
        assertThat(OffsetDateTime.parse(payload.occurredAt())).isNotNull();
    }
}
