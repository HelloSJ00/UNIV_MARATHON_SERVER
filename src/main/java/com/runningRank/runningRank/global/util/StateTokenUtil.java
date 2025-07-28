package com.runningRank.runningRank.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class StateTokenUtil {

    private final Key secretKey; // JWT 서명에 사용할 비밀 키
    private final long expirationMillis; // state 토큰의 유효 시간 (밀리초 단위)
    private final ObjectMapper objectMapper; // JWT 라이브러리가 JSON 처리에 ObjectMapper를 사용할 수 있음

    // 생성자를 통해 application.yml에서 설정 값을 주입받습니다.
    public StateTokenUtil(
            @Value("${strava.state-secret-key}") String secretKeyString, // application.yml에서 정의할 비밀 키 문자열
            @Value("${strava.state-expiration-millis}") long expirationMillis, // application.yml에서 정의할 만료 시간
            ObjectMapper objectMapper) { // ObjectMapper는 AppConfig에서 Bean으로 등록한 것을 사용
        // 주입받은 비밀 키 문자열을 바이트 배열로 변환하여 HMAC SHA-256 알고리즘용 키로 만듭니다.
        // 키는 최소 32바이트(256비트) 이상이어야 보안상 안전합니다.
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        this.expirationMillis = expirationMillis;
        this.objectMapper = objectMapper;
    }
    /**
     * 특정 사용자 ID를 포함하는 JWT 형태의 state 토큰을 생성합니다.
     * 이 토큰은 CSRF 방지용 랜덤 값(nonce)과 함께 사용자 ID를 안전하게 전달합니다.
     *
     * @param userId 토큰에 포함시킬 사용자의 고유 ID
     * @return 서명된 JWT 형태의 state 토큰 문자열
     */
    public String generateStateToken(Long userId) {
        String nonce = UUID.randomUUID().toString(); // CSRF 방지를 위한 예측 불가능한 랜덤 값
        long nowMillis = System.currentTimeMillis(); // 현재 시간 (밀리초)
        Date now = new Date(nowMillis); // 현재 Date 객체
        Date expiration = new Date(nowMillis + expirationMillis); // 만료 시간 계산

        // JWT 클레임(claims)에 포함시킬 정보들을 Map 형태로 정의합니다.
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId); // 사용자 ID
        claims.put("nonce", nonce); // 랜덤 nonce

        // Jwts.builder()를 사용하여 JWT를 구성하고 서명하여 최종 문자열을 반환합니다.
        return Jwts.builder()
                .setClaims(claims) // 위에서 정의한 클레임 포함
                .setIssuedAt(now) // 토큰 발행 시간
                .setExpiration(expiration) // 토큰 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS256) // 비밀 키와 서명 알고리즘을 사용하여 서명
                .compact(); // JWT를 압축하여 문자열 형태로 만듦
    }

    /**
     * 받은 state 토큰(JWT)을 검증하고, 유효하다면 그 안에 포함된 클레임(정보)들을 반환합니다.
     *
     * @param stateToken 검증할 state 토큰 문자열
     * @return 토큰에 포함된 클레임(Map 형태)
     * @throws SecurityException 토큰이 유효하지 않거나 만료되었을 경우
     */
    public Map<String, Object> validateStateToken(String stateToken) {
        try {
            // Jwts.parserBuilder()를 사용하여 토큰을 파싱하고 서명을 검증합니다.
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 토큰 서명에 사용된 비밀 키 지정
                    .build()
                    .parseClaimsJws(stateToken) // JWT 문자열 파싱
                    .getBody(); // 파싱된 JWT에서 클레임(body) 추출
        } catch (Exception e) {
            // 토큰 서명이 유효하지 않거나 (SignatureException), 토큰이 만료되었거나 (ExpiredJwtException),
            // 토큰 형식이 잘못되었을 (MalformedJwtException) 경우 등 다양한 JWT 관련 예외를 처리합니다.
            // SecurityException을 발생시켜 상위 호출자에게 유효하지 않은 토큰임을 알립니다.
            throw new SecurityException("Invalid or expired state token", e);
        }
    }
}
