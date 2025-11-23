package me.gg.pinit.pinittask.infrastructure.web;

import jakarta.annotation.PostConstruct;
import me.gg.pinit.pinittask.domain.member.model.Member;
import me.gg.pinit.pinittask.domain.member.repository.MemberRepository;
import me.gg.pinit.pinittask.interfaces.utils.MemberId;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Duration;
import java.time.ZoneId;

@Component
public class MemberIdForTestArgumentResolver implements HandlerMethodArgumentResolver {
    private final MemberRepository memberRepository;

    public MemberIdForTestArgumentResolver(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MemberId.class);
    }

    @Override
    public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return 1L;
    }

    @PostConstruct
    public void init() {
        memberRepository.save(new Member("sample", "sample", Duration.ofHours(4), ZoneId.of("Asia/Seoul")));
    }
}
