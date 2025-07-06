package com.runningRank.runningRank.auth.controller;


import com.runningRank.runningRank.auth.dto.*;
import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.auth.service.AuthService;
import com.runningRank.runningRank.auth.service.KakaoOAuthService;
import com.runningRank.runningRank.global.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestBody KakaoSignupRequest request) {
        return ResponseEntity.ok(kakaoOAuthService.kakaoSignUp(request));
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
     * PUT /api/users/update-user-info
     *
     * @param request UserUpdateRequest DTO (수정할 사용자 정보)
     * @return 수정 성공 여부를 나타내는 ResponseEntity<ApiResponse<Boolean>>
     */
    @PutMapping("/update-user-info")
    public ResponseEntity<ApiResponse<Boolean>> updateUserInfo(@RequestBody UserUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // HTTP 상태 코드 401
                    .body(ApiResponse.<Boolean>builder() // Boolean 타입 명시
                            .status(HttpStatus.UNAUTHORIZED.value()) // ApiResponse의 status 필드에 HTTP 상태 코드 값 설정
                            .message("인증되지 않은 사용자입니다.")
                            .build());
        }

        Object principal = authentication.getPrincipal();
        Long userId;

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            userId = userDetails.getId(); // CustomUserDetails에 getId() 메서드가 있다고 가정
        } else if (principal instanceof String) {
            try {
                userId = Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                System.err.println("인증된 Principal(String)이 유효한 ID 형식이 아닙니다: " + principal);
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED) // HTTP 상태 코드 401
                        .body(ApiResponse.<Boolean>builder() // Boolean 타입 명시
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("사용자 ID를 파싱할 수 없습니다.")
                                .build());
            }
        } else {
            System.err.println("인증된 Principal 타입이 CustomUserDetails 또는 String이 아닙니다: " + principal.getClass().getName());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // HTTP 상태 코드 401
                    .body(ApiResponse.<Boolean>builder() // Boolean 타입 명시
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("알 수 없는 사용자 인증 정보입니다.")
                            .build());
        }

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // HTTP 상태 코드 401
                    .body(ApiResponse.<Boolean>builder() // Boolean 타입 명시
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("인증된 사용자 ID를 찾을 수 없습니다.")
                            .build());
        }

        try {
            boolean isUpdated = authService.updateUserInfo(request, userId);

            if (isUpdated) {
                return ResponseEntity
                        .ok(ApiResponse.<Boolean>builder() // HTTP 상태 코드 200
                                .status(HttpStatus.OK.value())
                                .message("사용자 정보가 성공적으로 업데이트되었습니다.")
                                .data(true) // 성공 시 데이터로 true를 포함
                                .build());
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST) // HTTP 상태 코드 400
                        .body(ApiResponse.<Boolean>builder() // Boolean 타입 명시
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("사용자 정보 업데이트에 실패했습니다.")
                                .build());
            }
        } catch (EntityNotFoundException e) {
            System.err.println("사용자 정보 수정 실패: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // HTTP 상태 코드 404
                    .body(ApiResponse.<Boolean>builder() // Boolean 타입 명시
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage()) // 예외 메시지 포함
                            .build());
        } catch (RuntimeException e) {
            System.err.println("사용자 정보 수정 중 예상치 못한 오류 발생: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR) // HTTP 상태 코드 500
                    .body(ApiResponse.<Boolean>builder() // Boolean 타입 명시
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("서버 내부 오류가 발생했습니다.")
                            .build());
        }
    }
    }
