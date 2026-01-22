package me.gg.pinit.pinittask.interfaces.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.domain.task.model.TaskType;
import me.gg.pinit.pinittask.infrastructure.events.RabbitEventPublisher;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;
import me.gg.pinit.pinittask.interfaces.dto.TaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerV1IntegrationTest {

    private static final long MEMBER_ID = 3L;
    private static final ZoneId MEMBER_ZONE = ZoneId.of("Asia/Seoul");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    RabbitEventPublisher rabbitEventPublisher;

    @BeforeEach
    void setUpMember() {
        if (!memberRepository.existsById(MEMBER_ID)) {
            memberRepository.save(new Member(MEMBER_ID, "task-user", MEMBER_ZONE));
        }
    }

    @Test
    void taskLifecycle_create_retrieve_list_cursor_complete_reopen_delete() throws Exception {
        TaskRequest createRequest = new TaskRequest(
                "리포트 작성",
                "주간 리포트 초안 작성",
                new DateTimeWithZone(LocalDateTime.of(2024, 4, 1, 18, 0), MEMBER_ZONE),
                5,
                3,
                TaskType.QUICK_TASK,
                List.of(),
                List.of()
        );

        MvcResult createResult = mockMvc.perform(post("/v1/tasks")
                        .header("X-Member-Id", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ownerId").value(MEMBER_ID))
                .andExpect(jsonPath("$.title").value("리포트 작성"))
                .andExpect(jsonPath("$.dueDate.dateTime").value("2024-04-01T18:00:00"))
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long taskId = created.get("id").asLong();
        assertThat(taskId).isPositive();

        mockMvc.perform(get("/v1/tasks/{taskId}", taskId)
                        .header("X-Member-Id", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("리포트 작성"))
                .andExpect(jsonPath("$.taskType").value("QUICK_TASK"));

        mockMvc.perform(get("/v1/tasks")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("page", "0")
                        .param("size", "5")
                        .param("readyOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(taskId));

        MvcResult cursorResult = mockMvc.perform(get("/v1/tasks/cursor")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("size", "10")
                        .param("cursor", "2000-01-01T00:00:00|0")
                        .param("readyOnly", "true"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode cursorNode = objectMapper.readTree(cursorResult.getResponse().getContentAsString());
        assertThat(cursorNode.get("data").isArray()).isTrue();
        assertThat(cursorNode.get("data")).isNotEmpty();
        assertThat(cursorNode.get("data").get(0).get("id").asLong()).isEqualTo(taskId);
        assertThat(cursorNode.get("hasNext").asBoolean()).isFalse();

        mockMvc.perform(post("/v1/tasks/{taskId}/complete", taskId)
                        .header("X-Member-Id", MEMBER_ID))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/v1/tasks/{taskId}/reopen", taskId)
                        .header("X-Member-Id", MEMBER_ID))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/v1/tasks/{taskId}", taskId)
                        .header("X-Member-Id", MEMBER_ID)
                        .param("deleteSchedules", "false"))
                .andExpect(status().isNoContent());
    }
}
