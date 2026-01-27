package me.gg.pinit.pinittask.interfaces.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.repository.TaskRepository;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import me.gg.pinit.pinittask.infrastructure.events.RabbitEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TaskArchiveControllerV2IntegrationTest.FixedClockConfig.class)
@Transactional
class TaskArchiveControllerV2IntegrationTest {

    private static final long MEMBER_ID = 44L;
    private static final ZoneOffset MEMBER_OFFSET = ZoneOffset.of("+09:00");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    RabbitEventPublisher rabbitEventPublisher;

    @BeforeEach
    void setUpMember() {
        if (!memberRepository.existsById(MEMBER_ID)) {
            memberRepository.save(new Member(MEMBER_ID, "archive-user", MEMBER_OFFSET));
        }
    }

    @Test
    void completedArchive_paginatesWithCursorAndRespectsCutoff() throws Exception {
        Task excluded = completedTask(LocalDate.of(2025, 1, 10)); // cutoff+1 -> 제외
        Task first = completedTask(LocalDate.of(2025, 1, 9));     // cutoff 포함
        Task second = completedTask(LocalDate.of(2025, 1, 8));
        taskRepository.saveAll(List.of(excluded, first, second));

        var firstResponse = mockMvc.perform(get("/v2/tasks/completed")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].dueDate.date").value("2025-01-09"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andReturn();

        JsonNode firstNode = objectMapper.readTree(firstResponse.getResponse().getContentAsString());
        String cursor = firstNode.get("nextCursor").asText();
        assertThat(cursor).isNotBlank();

        mockMvc.perform(get("/v2/tasks/completed")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("size", "1")
                        .param("cursor", cursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].dueDate.date").value("2025-01-08"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void completedArchive_usesRequestOffsetForCutoff() throws Exception {
        Task include = completedTask(LocalDate.of(2025, 1, 8));
        Task exclude = completedTask(LocalDate.of(2025, 1, 9)); // cutoff with -05:00 is 2025-01-08
        taskRepository.saveAll(List.of(include, exclude));

        mockMvc.perform(get("/v2/tasks/completed")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("offset", "-05:00")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].dueDate.date").value("2025-01-08"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void completedArchive_rejectsInvalidSize() throws Exception {
        mockMvc.perform(get("/v2/tasks/completed")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    private Task completedTask(LocalDate deadlineDate) {
        ZonedDateTime deadline = deadlineDate.atStartOfDay(MEMBER_OFFSET);
        Task task = new Task(MEMBER_ID, "archive", "desc", new TemporalConstraint(deadline, Duration.ZERO), new ImportanceConstraint(1, 1));
        task.markCompleted();
        return task;
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2025-01-10T00:00:00Z"), ZoneOffset.UTC);
        }
    }
}
