package com.runningRank.runningRank.auth.controller;


import com.runningRank.runningRank.auth.dto.*;
import com.runningRank.runningRank.auth.service.AuthService;
import com.runningRank.runningRank.auth.service.KakaoOAuthService;
import com.runningRank.runningRank.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoOAuthService kakaoOAuthService;

    /**
     * 회원가입 API
     * @param request
     * @return
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    /**
     * 로그인 API
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * 카카오 로그인
     */
    @PostMapping("/oauth/kakao")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(kakaoOAuthService.kakaoLogin(request.getCode()));
    }

    /**
     * 카카오 회원가입
     */
    @PostMapping("/oauth/kakao/signup")
    public ResponseEntity<UserResponse> kakaoLogin(@RequestBody KakaoSignupRequest request) {
        return ResponseEntity.ok(kakaoOAuthService.kakaoSignup(request));
    }

    /**
     *
     */
    @GetMapping("/university/all")
    public ResponseEntity<ApiResponse<List<String>>> getAllUniversityNames() {
        List<String> universityNames = authService.getAllUniversityNames();
        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .status(HttpStatus.OK.value())
                .message("모든 학교 조회 성공")
                .data(universityNames)
                .build());
    }
}
