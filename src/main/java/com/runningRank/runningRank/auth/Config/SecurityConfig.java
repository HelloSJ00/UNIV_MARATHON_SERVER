package com.runningRank.runningRank.auth.Config;

//import com.runningRank.runningRank.auth.jwt.JwtAuthenticationFilter;
import com.runningRank.runningRank.auth.jwt.JwtAuthenticationFilter;
import com.runningRank.runningRank.auth.jwt.JwtProvider;
import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // REST API니까 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 X
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",        // 로그인, 회원가입 ,카카오 OAuth 콜백 등
                                "/api/auth/oauth/kakao", // ✅ 이 라인 추가
                                "/swagger-ui/**",    // Swagger 문서
                                "/v3/api-docs/**",
                                "/api/major/**",
                                "/api/runningRecord/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/test/badge"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider,userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
