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

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailDuplicate(
            @RequestParam("email") String email
    ) {
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .message("이메일 중복 여부 확인 성공")
                .data(authService.checkEmailDuplicate(email))
                .build());
    }

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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
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
     * 회원가입시 모든 학교 조회
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

    /**
     * 학교별 모든 전공 조회
     * @return
     */
    @GetMapping("/university/major")
    public ResponseEntity<ApiResponse<List<String>>> getMajorsByUniversityName(@RequestParam String universityName) {
        try {
            List<String> majors = authService.getMajorsByUniversityName(universityName);
            return ResponseEntity.ok(
                    ApiResponse.<List<String>>builder()
                            .status(HttpStatus.OK.value())
                            .message("전공 조회 성공")
                            .data(majors)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.<List<String>>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("존재하지 않는 학교입니다.")
                            .data(null)
                            .build()
                    );
        }
    }
}
