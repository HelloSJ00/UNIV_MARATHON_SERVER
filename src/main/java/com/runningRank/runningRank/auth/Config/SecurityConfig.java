package com.runningRank.runningRank.auth.Config;

import com.runningRank.runningRank.auth.jwt.JwtAuthenticationFilter;
import com.runningRank.runningRank.auth.jwt.JwtProvider;
import com.runningRank.runningRank.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
    public SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/auth/oauth/kakao",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/major/**",
                                "/api/runningRecord/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/test/badge",
                                "/api/user/upload-url"
                        ).permitAll()
                // /api/admin 경로는 'ADMIN' 역할을 가진 사용자만 접근 가능하도록 추가
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // --- 여기를 수정합니다 ---
        config.setAllowedOrigins(List.of(
                "http://localhost:3000", // 로컬 개발 환경
                "https://univ-marathon-rank-client-isskon42k-hellosj00s-projects.vercel.app" ,// Vercel 배포 도메인,
                "https://univ-marathon-rank-client.vercel.app/",
                "https://www.univmarathon.com/"
                // 다른 운영 환경 도메인이 있다면 여기에 추가
        ));
        // --- 수정 끝 ---

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
        config.setAllowCredentials(true); // 자격 증명 (쿠키, Authorization 헤더 등) 허용
        config.setMaxAge(3600L); // Pre-flight 요청 캐싱 시간 (1시간)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 CORS 설정 적용

        return source;
    }


}