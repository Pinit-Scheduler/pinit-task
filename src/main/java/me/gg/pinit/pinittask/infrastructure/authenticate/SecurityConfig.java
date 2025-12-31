package me.gg.pinit.pinittask.infrastructure.authenticate;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.PublicKey;

@Configuration
public class SecurityConfig {

    @Value("${path.key.jwt.public}")
    private String jwtPublicKeyPath;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        (request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing token")
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/liveness", "/actuator/health/readiness", "/v3/**", "/swagger-ui/**", "/async-api/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(
                jwtAuthenticationFilter(authenticationManager),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new JwtAuthenticationFilter(authenticationManager);
    }

    @Bean
    public AuthenticationManager authenticationManager(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new ProviderManager(jwtAuthenticationProvider);
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationProvider(jwtTokenProvider);
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        PublicKey publicKey = RsaKeyProvider.loadPublicKey(jwtPublicKeyPath);
        return new JwtTokenProvider(publicKey);
    }
}
