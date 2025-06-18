package com.runningRank.runningRank.auth.controller;


import com.runningRank.runningRank.auth.dto.*;
import com.runningRank.runningRank.auth.service.AuthService;
import com.runningRank.runningRank.auth.service.KakaoOAuthService;
import com.runningRank.runningRank.global.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
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
                .data(!authService.checkEmailDuplicate(email))
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

    /**
     * 사용자 정보를 수정하는 API 엔드포인트.
     * PUT /api/users/{userId}
     *
     * @param userId  수정 대상 사용자의 ID
     * @param request UserUpdateRequest DTO (수정할 사용자 정보)
     * @return 수정 성공 여부를 나타내는 ResponseEntity
     */
    @PutMapping("/{userId}") // PUT 메서드, 특정 사용자 ID를 경로 변수로 받음
    public ResponseEntity<Void> updateUserInfo(@PathVariable("userId") Long userId,
                                               @RequestBody UserUpdateRequest request) {
        try {
            // UserService를 통해 사용자 정보 수정 로직 호출
            boolean isUpdated = authService.updateUserInfo(request, userId);

            if (isUpdated) {
                return ResponseEntity.noContent().build(); // 204 No Content: 성공했지만 응답 본문은 없음
            } else {
                // 이론적으로 updateUserInfo는 항상 true를 반환하지만,
                // 만약을 위한 폴백 (예: 특정 조건 미충족 시 false 반환하도록 서비스 변경 시)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request
            }
        } catch (EntityNotFoundException e) {
            // 사용자를 찾을 수 없거나, 대학교/전공을 찾을 수 없을 때 (UserService에서 throw 함)
            System.err.println("사용자 정보 수정 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        } catch (RuntimeException e) {
            // 그 외 서비스 로직에서 발생할 수 있는 일반적인 런타임 예외 처리
            System.err.println("사용자 정보 수정 중 예상치 못한 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }
}
