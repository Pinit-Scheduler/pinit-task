package me.gg.pinit.pinittask.interfaces.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.infrastructure.events.RabbitEventPublisher;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;
import me.gg.pinit.pinittask.interfaces.dto.ScheduleSimpleRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ScheduleControllerV1IntegrationTest {

    private static final long MEMBER_ID = 1L;
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
            memberRepository.save(new Member(MEMBER_ID, "tester", MEMBER_ZONE));
        }
    }

    @Test
    void createAndRetrieveSimpleSchedule() throws Exception {
        ScheduleSimpleRequest request = new ScheduleSimpleRequest(
                "팀 회의",
                "주간 회의",
                new DateTimeWithZone(LocalDateTime.of(2024, 1, 1, 9, 0), MEMBER_ZONE)
        );

        MvcResult createResult = mockMvc.perform(post("/v1/schedules")
                        .header("X-Member-Id", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ownerId").value(MEMBER_ID))
                .andExpect(jsonPath("$.title").value("팀 회의"))
                .andExpect(jsonPath("$.date.dateTime").value("2024-01-01T00:00:00"))
                .andExpect(jsonPath("$.state").value("NOT_STARTED"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long scheduleId = created.get("id").asLong();
        assertThat(scheduleId).isPositive();

        mockMvc.perform(get("/v1/schedules/{scheduleId}", scheduleId)
                        .header("X-Member-Id", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(scheduleId))
                .andExpect(jsonPath("$.title").value("팀 회의"))
                .andExpect(jsonPath("$.ownerId").value(MEMBER_ID));

        mockMvc.perform(get("/v1/schedules")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("time", "2024-01-01T00:00:00")
                        .param("zoneId", MEMBER_ZONE.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(scheduleId))
                .andExpect(jsonPath("$[0].title").value("팀 회의"))
                .andExpect(jsonPath("$[0].date.dateTime").value("2024-01-01T00:00:00"));
    }
}
