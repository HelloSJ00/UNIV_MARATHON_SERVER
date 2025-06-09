//package com.runningRank.runningRank.auth.jwt;
//
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.oauth2.jwt.JwtException;
//import org.springframework.stereotype.Component;
//import org.springframework.beans.factory.annotation.Value;
//import java.security.Key;
//import java.util.Date;
//
//@Slf4j
//@Component
//public class JwtProvider {
//    @Value("${jwt.secret}")
//    private String secretKey;
//
//    @Value("${jwt.access-token-expiration}")
//    private long accessTokenExpiration; // milliseconds
//
//    @Value("${jwt.refresh-token-expiration}")
//    private long refreshTokenExpiration;
//
//    private Key key;
//
//    @PostConstruct
//    protected void init() {
//        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
//    }
//
//    // ✅ AccessToken 생성
//    public String generateAccessToken(String subject) {
//        return Jwts.builder()
//                .setSubject(subject)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // ✅ RefreshToken 생성
//    public String generateRefreshToken() {
//        return Jwts.builder()
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // ✅ 토큰에서 subject(email or userId) 꺼내기
//    public String getSubject(String token) {
//        return parseClaims(token).getBody().getSubject();
//    }
//
//    // ✅ 토큰 유효성 검증
//    public boolean validateToken(String token) {
//        try {
//            parseClaims(token);
//            return true;
//        } catch (ExpiredJwtException e) {
//            log.warn("만료된 JWT 토큰입니다.");
//        } catch (JwtException | IllegalArgumentException e) {
//            log.warn("잘못된 JWT 토큰입니다.");
//        }
//        return false;
//    }
//
//    private Jws<Claims> parseClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(this.key)
//                .build()
//                .parseClaimsJws(token);
//    }
//}
