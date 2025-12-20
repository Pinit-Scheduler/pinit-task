package me.gg.pinit.pinittask;

import me.gg.pinit.pinittask.infrastructure.authenticate.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
class PinitTaskApplicationTests {

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Test
    void contextLoads() {
    }
}
