package com.runningRank.runningRank.auth.jwt;

import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod; // HttpMethod 임포트
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Request URI 로깅 (디버깅용)
        log.debug("Request URI: {}", request.getRequestURI());

        // --- 핵심 추가 부분 시작 ---
        // Preflight 요청 (OPTIONS 메서드)은 JWT 인증 로직을 건너뜁니다.
        // 브라우저는 실제 요청을 보내기 전에 OPTIONS 요청을 보내 CORS 정책을 확인합니다.
        // 이 요청은 인증이 필요 없으며, CORS 필터가 처리하도록 해야 합니다.
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            log.debug("OPTIONS request detected. Skipping JWT authentication.");
            filterChain.doFilter(request, response); // 다음 필터로 바로 넘김
            return; // 메서드 종료 (JWT 인증 로직을 실행하지 않음)
        }
        // --- 핵심 추가 부분 끝 ---

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader ); // 디버깅용
        log.debug("Authorization Header: {}", authHeader);

        // Authorization 헤더가 있을 때만 토큰 검증 수행
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Extracted JWT Token: {}", token);

            if (jwtProvider.validateToken(token)) {
                log.debug("JWT token is valid");
                String email = jwtProvider.getSubject(token);
                log.debug("Extracted subject(email): {}", email);

                CustomUserDetails userDetails = userDetailsService.loadUserByUsername(email);
                log.debug("Loaded UserDetails: {}", userDetails.getUsername());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authentication object set in SecurityContext");

                filterChain.doFilter(request, response); // 다음 필터로 정상 전달
                return;
            } else {
                log.warn("Invalid JWT token: {}", token);

                // --- 유효하지 않은 토큰에만 401 반환 ---
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized: Invalid token.\"}");
                return;
            }
        }

        // --- Authorization 헤더가 없는 경우 필터 체인 그대로 진행 ---
        log.debug("No JWT token found in Authorization header");
        filterChain.doFilter(request, response);
    }
}