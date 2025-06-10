package com.runningRank.runningRank.auth.jwt;

//import com.runningRank.runningRank.auth.service.CustomUserDetailsService;
import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService; // 사용자 정보 로드용

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader );
        log.debug("Authorization Header: {}", authHeader);

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
            } else {
                log.warn("Invalid JWT token: {}", token);
            }
        } else {
            log.debug("No JWT token found in Authorization header");
        }

        filterChain.doFilter(request, response);
    }
}
