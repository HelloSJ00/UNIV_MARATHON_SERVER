package com.runningRank.runningRank.emailVerification.controller;

import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.emailVerification.service.EmailVerificationService;
import com.runningRank.runningRank.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emailVerification")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * 사용자가 인증하고픈 대학의 도메인과 사용자 대학의 메일 도메인이 일치하는지
     * @param email
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Boolean>> requestUniversityEmailVerification(
            @RequestParam("email") String email
    ) {
        // 🔐 현재 로그인한 유저의 ID를 SecurityContext에서 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // 이메일 도메인 검증 + 인증 메일 전송
        boolean isRequested = emailVerificationService.isEmailDomainMatchedWithUniv(userId, email);

        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .status(HttpStatus.OK.value()) // 200
                .message("학교 이메일 인증 요청 완료")
                .data(isRequested)
                .build();

        return ResponseEntity.ok(response);
    }

}
