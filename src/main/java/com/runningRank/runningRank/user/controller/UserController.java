package com.runningRank.runningRank.user.controller;

import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.user.dto.PresignedUrlRequest;
import com.runningRank.runningRank.user.dto.PresignedUrlResponse;
import com.runningRank.runningRank.user.dto.UserVerification;
import com.runningRank.runningRank.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("Hello " + auth.getName());
    }

    @GetMapping("/verifications")
    public ResponseEntity<ApiResponse<List<UserVerification>>> getUserVerifications() {
        // 🔐 현재 로그인한 유저의 ID를 SecurityContext에서 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        return ResponseEntity.ok(ApiResponse.<List<UserVerification>>builder()
                .status(HttpStatus.OK.value())
                .message("유저 인증 기록 조회 성공")
                .data(userService.getUserVerifications(userId))
                .build());
    }

    /**
     * 클라이언트가 S3에 파일을 직접 업로드할 수 있도록 Presigned URL을 생성하는 API 엔드포인트.
     * POST /api/users/upload-url
     *
     * @param request 파일명과 파일 타입을 포함하는 요청 바디
     * @return 생성된 Presigned URL과 최종 파일 접근 URL을 포함하는 ResponseEntity
     */
    @PostMapping("/upload-url")
    public ResponseEntity<PresignedUrlResponse> generateUploadUrl(@RequestBody PresignedUrlRequest request) {
        try {
            // UserService를 통해 Presigned URL 생성 로직 호출
            PresignedUrlResponse response = userService.generatePresignedUrl(request);
            return ResponseEntity.ok(response); // 200 OK와 함께 응답 반환
        } catch (RuntimeException e) {
            // UserService에서 발생한 예외를 처리 (예: URISyntaxException)
            // 실제 환경에서는 더 구체적인 예외 처리 및 에러 메시지 반환 필요
            System.err.println("Presigned URL 생성 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }
}
