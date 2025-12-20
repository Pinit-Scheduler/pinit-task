package me.gg.pinit.pinittask;

import me.gg.pinit.pinittask.infrastructure.authenticate.JwtTokenProvider;
import me.gg.pinit.pinittask.utils.TestKeys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class PinitTaskApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestBeans {
        @Bean
        @Primary
        JwtTokenProvider testJwtTokenProvider() {
            return new JwtTokenProvider(TestKeys.publicKey());
        }
    }
}
