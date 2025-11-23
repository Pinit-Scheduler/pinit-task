package me.gg.pinit.pinittask.interfaces.config;

import me.gg.pinit.pinittask.infrastructure.web.MemberIdForTestArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final MemberIdForTestArgumentResolver memberIdForTestArgumentResolver;

    public WebConfig(MemberIdForTestArgumentResolver memberIdForTestArgumentResolver) {
        this.memberIdForTestArgumentResolver = memberIdForTestArgumentResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173") // 허용할 origin 목록
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberIdForTestArgumentResolver);
    }
}
