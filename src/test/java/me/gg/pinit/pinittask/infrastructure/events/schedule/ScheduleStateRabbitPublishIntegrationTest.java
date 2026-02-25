package me.gg.pinit.pinittask.infrastructure.events.schedule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleStateChangeService;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.model.ScheduleType;
import me.gg.pinit.pinittask.domain.schedule.repository.ScheduleRepository;
import me.gg.pinit.pinittask.infrastructure.events.support.RabbitMqTestcontainersSupport;
import org.junit.jupiter.api.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Tag("rabbitmq-integration")
class ScheduleStateRabbitPublishIntegrationTest extends RabbitMqTestcontainersSupport {

    private static final Long MEMBER_ID = 77001L;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final ZonedDateTime BASE_TIME = ZonedDateTime.of(2026, 2, 1, 9, 0, 0, 0, ZONE_ID);

    @Autowired
    ScheduleStateChangeService scheduleStateChangeService;
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ObjectMapper objectMapper;

    private String testQueueName;

    @BeforeEach
    void setUp() {
        assumeRabbitAvailable();
        Assumptions.assumeTrue(rabbitMQContainer.isRunning(), "RabbitMQ container is not running");

        scheduleRepository.deleteAll();
        memberRepository.deleteAll();

        memberRepository.save(new Member(MEMBER_ID, "rabbit-user", ZONE_ID));
        testQueueName = "test.schedule.state.events." + UUID.randomUUID();
        declareTestQueueAndBindings(
                ScheduleMessaging.RK_SCHEDULE_STARTED,
                ScheduleMessaging.RK_SCHEDULE_COMPLETED,
                ScheduleMessaging.RK_SCHEDULE_CANCELED
        );
        purgeQueue();
        DomainEvents.getEventsAndClear();
    }

    @AfterEach
    void cleanUpQueue() {
        if (testQueueName != null) {
            amqpAdmin.deleteQueue(testQueueName);
        }
        DomainEvents.getEventsAndClear();
    }

    @Test
    void startSchedule_publishesStartedMessage() throws IOException {
        Schedule schedule = saveNotStartedSchedule();

        scheduleStateChangeService.startSchedule(MEMBER_ID, schedule.getId(), BASE_TIME.plusHours(1));

        Message message = receiveMessage(MESSAGE_TIMEOUT_MS);
        assertThat(message).isNotNull();
        assertThat(message.getMessageProperties().getReceivedRoutingKey()).isEqualTo(ScheduleMessaging.RK_SCHEDULE_STARTED);

        JsonNode payload = objectMapper.readTree(message.getBody());
        assertThat(payload.get("scheduleId").asLong()).isEqualTo(schedule.getId());
        assertThat(payload.get("ownerId").asLong()).isEqualTo(MEMBER_ID);
        assertThat(payload.get("beforeState").asText()).isEqualTo("NOT_STARTED");
        assertThat(payload.get("occurredAt").asText()).isNotBlank();
        assertThat(payload.get("idempotentKey").asText()).isNotBlank();
    }

    @Test
    void completeSchedule_publishesCompletedMessage() throws IOException {
        Schedule schedule = saveNotStartedSchedule();

        scheduleStateChangeService.completeSchedule(MEMBER_ID, schedule.getId(), BASE_TIME.plusHours(2));

        Message message = receiveMessage(MESSAGE_TIMEOUT_MS);
        assertThat(message).isNotNull();
        assertThat(message.getMessageProperties().getReceivedRoutingKey()).isEqualTo(ScheduleMessaging.RK_SCHEDULE_COMPLETED);

        JsonNode payload = objectMapper.readTree(message.getBody());
        assertThat(payload.get("scheduleId").asLong()).isEqualTo(schedule.getId());
        assertThat(payload.get("ownerId").asLong()).isEqualTo(MEMBER_ID);
        assertThat(payload.get("beforeState").asText()).isEqualTo("NOT_STARTED");
        assertThat(payload.get("occurredAt").asText()).isNotBlank();
        assertThat(payload.get("idempotentKey").asText()).isNotBlank();
    }

    @Test
    void cancelSchedule_publishesCanceledMessage() throws IOException {
        Schedule schedule = saveNotStartedSchedule();

        scheduleStateChangeService.startSchedule(MEMBER_ID, schedule.getId(), BASE_TIME.plusHours(1));
        assertThat(receiveMessage(MESSAGE_TIMEOUT_MS)).isNotNull();

        scheduleStateChangeService.cancelSchedule(MEMBER_ID, schedule.getId());

        Message message = receiveMessage(MESSAGE_TIMEOUT_MS);
        assertThat(message).isNotNull();
        assertThat(message.getMessageProperties().getReceivedRoutingKey()).isEqualTo(ScheduleMessaging.RK_SCHEDULE_CANCELED);

        JsonNode payload = objectMapper.readTree(message.getBody());
        assertThat(payload.get("scheduleId").asLong()).isEqualTo(schedule.getId());
        assertThat(payload.get("ownerId").asLong()).isEqualTo(MEMBER_ID);
        assertThat(payload.get("beforeState").asText()).isEqualTo("IN_PROGRESS");
        assertThat(payload.get("occurredAt").asText()).isNotBlank();
        assertThat(payload.get("idempotentKey").asText()).isNotBlank();
    }

    @Test
    void suspendSchedule_doesNotPublishStateMessage() {
        Schedule schedule = saveNotStartedSchedule();

        scheduleStateChangeService.startSchedule(MEMBER_ID, schedule.getId(), BASE_TIME.plusHours(1));
        assertThat(receiveMessage(MESSAGE_TIMEOUT_MS)).isNotNull();

        scheduleStateChangeService.suspendSchedule(MEMBER_ID, schedule.getId(), BASE_TIME.plusHours(2));

        Message message = receiveMessage(NO_MESSAGE_TIMEOUT_MS);
        assertThat(message).isNull();
    }

    @Test
    void startSchedule_whenOwnerMismatch_doesNotPublishMessage() {
        Schedule schedule = saveNotStartedSchedule();

        assertThatThrownBy(() -> scheduleStateChangeService.startSchedule(MEMBER_ID + 1000, schedule.getId(), BASE_TIME.plusHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member does not own the schedule");

        Message message = receiveMessage(NO_MESSAGE_TIMEOUT_MS);
        assertThat(message).isNull();
    }

    @Test
    void cancelSchedule_whenNotStarted_throwsAndDoesNotPublishMessage() {
        Schedule schedule = saveNotStartedSchedule();

        assertThatThrownBy(() -> scheduleStateChangeService.cancelSchedule(MEMBER_ID, schedule.getId()))
                .isInstanceOf(IllegalTransitionException.class);

        Message message = receiveMessage(NO_MESSAGE_TIMEOUT_MS);
        assertThat(message).isNull();
    }

    @Test
    void startSchedule_whenRoutingKeyNotBound_doesNotReachQueue() {
        Schedule schedule = saveNotStartedSchedule();
        recreateQueueWithBindings(List.of(ScheduleMessaging.RK_SCHEDULE_COMPLETED));
        purgeQueue();

        scheduleStateChangeService.startSchedule(MEMBER_ID, schedule.getId(), BASE_TIME.plusHours(1));

        Message message = receiveMessage(NO_MESSAGE_TIMEOUT_MS);
        assertThat(message).isNull();
    }

    private Schedule saveNotStartedSchedule() {
        Schedule schedule = new Schedule(
                MEMBER_ID,
                null,
                "mq-test",
                "state-event",
                BASE_TIME,
                ScheduleType.DEEP_WORK
        );
        Schedule saved = scheduleRepository.save(schedule);
        DomainEvents.getEventsAndClear();
        purgeQueue();
        return saved;
    }

    private void recreateQueueWithBindings(List<String> routingKeys) {
        amqpAdmin.deleteQueue(testQueueName);
        declareTestQueueAndBindings(routingKeys.toArray(String[]::new));
    }

    private void declareTestQueueAndBindings(String... routingKeys) {
        Queue queue = QueueBuilder.nonDurable(testQueueName)
                .build();
        amqpAdmin.declareQueue(queue);

        for (String routingKey : routingKeys) {
            bindRoutingKey(routingKey);
        }
    }

    private void bindRoutingKey(String routingKey) {
        Binding binding = BindingBuilder.bind(new Queue(testQueueName))
                .to(new DirectExchange(ScheduleMessaging.DIRECT_EXCHANGE))
                .with(routingKey);
        amqpAdmin.declareBinding(binding);
    }

    private void purgeQueue() {
        amqpAdmin.purgeQueue(testQueueName, true);
        while (rabbitTemplate.receive(testQueueName) != null) {
            // drain leftovers that may still arrive asynchronously
        }
    }

    private Message receiveMessage(long timeoutMillis) {
        return rabbitTemplate.receive(testQueueName, timeoutMillis);
    }
}
