package me.gg.pinit.pinittask.interfaces.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.domain.task.model.TaskType;
import me.gg.pinit.pinittask.infrastructure.events.RabbitEventPublisher;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;
import me.gg.pinit.pinittask.interfaces.dto.ScheduleRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ScheduleControllerV0IntegrationTest {

    private static final long MEMBER_ID = 2L;
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
            memberRepository.save(new Member(MEMBER_ID, "legacy-user", MEMBER_ZONE));
        }
    }

    @Test
    void createLegacyScheduleAndListWithTaskDetails() throws Exception {
        ScheduleRequest request = new ScheduleRequest(
                "스터디 준비",
                "발표 자료 정리",
                null,
                new DateTimeWithZone(LocalDateTime.of(2024, 3, 1, 18, 0), MEMBER_ZONE),
                5,
                3,
                TaskType.QUICK_TASK,
                new DateTimeWithZone(LocalDateTime.of(2024, 2, 28, 9, 0), MEMBER_ZONE),
                List.of(),
                List.of()
        );

        MvcResult createResult = mockMvc.perform(post("/v0/schedules")
                        .header("X-Member-Id", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.taskId").isNumber())
                .andExpect(jsonPath("$.title").value("스터디 준비"))
                .andExpect(jsonPath("$.deadline.dateTime").value("2024-03-01T18:00:00"))
                .andExpect(jsonPath("$.deadline.zoneId").value(MEMBER_ZONE.getId()))
                .andExpect(jsonPath("$.importance").value(5))
                .andExpect(jsonPath("$.difficulty").value(3))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long scheduleId = created.get("id").asLong();
        assertThat(scheduleId).isPositive();

        mockMvc.perform(get("/v0/schedules")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("time", "2024-02-28T00:00:00")
                        .param("zoneId", MEMBER_ZONE.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(scheduleId))
                .andExpect(jsonPath("$[0].taskId").isNumber())
                .andExpect(jsonPath("$[0].importance").value(5))
                .andExpect(jsonPath("$[0].date.dateTime").value("2024-02-28T00:00:00"));
    }
}
