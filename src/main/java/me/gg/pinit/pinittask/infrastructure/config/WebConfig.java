package me.gg.pinit.pinittask.infrastructure.config;

import me.gg.pinit.pinittask.infrastructure.web.EventQueueClearInterceptor;
import me.gg.pinit.pinittask.infrastructure.web.MemberIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MemberIdArgumentResolver memberIdArgumentResolver;
    private final EventQueueClearInterceptor eventQueueClearInterceptor;

    public WebConfig(MemberIdArgumentResolver memberIdArgumentResolver, EventQueueClearInterceptor eventQueueClearInterceptor) {
        this.memberIdArgumentResolver = memberIdArgumentResolver;
        this.eventQueueClearInterceptor = eventQueueClearInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberIdArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(eventQueueClearInterceptor);
    }
}
