package me.gg.pinit.pinittask.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Pinit Task API",
                version = "v2.1-timezone",
                description = "일정/작업 관리와 의존 관계 기능을 제공하는 API",
                contact = @Contact(name = "Pinit Team", email = "support@pinit.local")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local"),
                @Server(url = "https://api.pinit.go-gradually.me", description = "Production")
        }
)
public class OpenApiConfig {
}
