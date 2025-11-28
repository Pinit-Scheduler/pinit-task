package me.gg.pinit.pinittask.interfaces.config;

import me.gg.pinit.pinittask.infrastructure.web.MemberIdForTestArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final MemberIdForTestArgumentResolver memberIdForTestArgumentResolver;

    private final CorsProperties corsProperties;

    public WebConfig(MemberIdForTestArgumentResolver memberIdForTestArgumentResolver, CorsProperties corsProperties) {
        this.memberIdForTestArgumentResolver = memberIdForTestArgumentResolver;
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(corsProperties.getAllowedHeaders().toArray(new String[0]))
                .allowCredentials(corsProperties.getAllowCredentials())
                .maxAge(corsProperties.getMaxAge());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberIdForTestArgumentResolver);
    }
}
