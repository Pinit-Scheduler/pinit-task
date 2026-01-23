package me.gg.pinit.pinittask.interfaces.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.infrastructure.events.RabbitEventPublisher;
import me.gg.pinit.pinittask.interfaces.dto.DateWithOffset;
import me.gg.pinit.pinittask.interfaces.task.dto.TaskCreateRequestV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerV2IntegrationTest {

    private static final long MEMBER_ID = 4L;
    private static final ZoneOffset OFFSET = ZoneOffset.of("+09:00");

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
            memberRepository.save(new Member(MEMBER_ID, "task-v2-user", OFFSET));
        }
    }

    @Test
    void createAndFetchTasksWithDateOffsetCursor() throws Exception {
        TaskCreateRequestV2 createRequest = new TaskCreateRequestV2(
                "리포트 작성",
                "주간 리포트 초안 작성",
                new DateWithOffset(java.time.LocalDate.of(2024, 4, 1), OFFSET),
                5,
                3,
                List.of()
        );

        var createResult = mockMvc.perform(post("/v2/tasks")
                        .header("X-Member-Id", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate.date").value("2024-04-01"))
                .andExpect(jsonPath("$.dueDate.offset").value("+09:00"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long taskId = created.get("id").asLong();

        mockMvc.perform(get("/v2/tasks")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(taskId))
                .andExpect(jsonPath("$.content[0].dueDate.date").value("2024-04-01"))
                .andExpect(jsonPath("$.content[0].dueDate.offset").value("+09:00"));

        var cursorResult = mockMvc.perform(get("/v2/tasks/cursor")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("size", "10")
                        .param("cursor", "2000-01-01T00:00:00|0")
                        .param("readyOnly", "true"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode cursorNode = objectMapper.readTree(cursorResult.getResponse().getContentAsString());
        assertThat(cursorNode.get("data").isArray()).isTrue();
        assertThat(cursorNode.get("data").get(0).get("dueDate").get("date").asText()).isEqualTo("2024-04-01");
        assertThat(cursorNode.get("data").get(0).get("dueDate").get("offset").asText()).isEqualTo("+09:00");
    }
}
