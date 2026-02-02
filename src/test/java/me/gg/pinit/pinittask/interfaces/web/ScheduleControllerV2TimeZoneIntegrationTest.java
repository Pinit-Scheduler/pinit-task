package me.gg.pinit.pinittask.interfaces.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.domain.schedule.model.ScheduleType;
import me.gg.pinit.pinittask.infrastructure.events.RabbitEventPublisher;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;
import me.gg.pinit.pinittask.interfaces.schedule.dto.ScheduleSimpleRequest;
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

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ScheduleControllerV2TimeZoneIntegrationTest {

    private static final long MEMBER_ID = 55L;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

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
            memberRepository.save(new Member(MEMBER_ID, "tz-user", DEFAULT_ZONE));
        }
    }

    @Test
    void scheduleIsRenderedInRequestedTimeZone() throws Exception {
        ScheduleSimpleRequest request = new ScheduleSimpleRequest(
                "심야 작업",
                "시간대 테스트",
                new DateTimeWithZone(LocalDateTime.of(2026, 2, 1, 9, 0), DEFAULT_ZONE),
                ScheduleType.DEEP_WORK
        );

        mockMvc.perform(post("/v2/schedules")
                        .header("X-Member-Id", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // KST 뷰: 원본 입력과 동일하게 09:00 표시
        mockMvc.perform(get("/v2/schedules")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("time", "2026-02-01T12:00:00")
                        .param("zoneId", DEFAULT_ZONE.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date.dateTime").value("2026-02-01T09:00:00"))
                .andExpect(jsonPath("$[0].date.zoneId").value(DEFAULT_ZONE.getId()));

        // 미국 서부 뷰: 같은 일정이 전날 16:00로 보인다.
        mockMvc.perform(get("/v2/schedules")
                        .header("X-Member-Id", MEMBER_ID)
                        .param("time", "2026-01-31T12:00:00")
                        .param("zoneId", "America/Los_Angeles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date.dateTime").value("2026-01-31T16:00:00"))
                .andExpect(jsonPath("$[0].date.zoneId").value("America/Los_Angeles"));
    }
}
