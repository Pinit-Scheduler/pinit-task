package me.gg.pinit.pinittask.infrastructure.authenticate;

import jakarta.servlet.http.HttpServletResponse;
import me.gg.pinit.pinittask.interfaces.config.CorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.PublicKey;

@Configuration
public class SecurityConfig {
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
                        .requestMatchers("/login", "/signup", "/refresh", "/login/**", "/v3/**", "/swagger-ui/**").permitAll()
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        PublicKey publicKey = RsaKeyProvider.loadPublicKey("keys/public_key.pem");
        return new JwtTokenProvider(publicKey, "https://pinit.go-gradually.me");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(corsProperties.getAllowedMethods());
        config.setAllowedHeaders(corsProperties.getAllowedHeaders());
        config.setAllowCredentials(corsProperties.getAllowCredentials());
        config.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
