package com.runningRank.runningRank.user.controller;

import com.runningRank.runningRank.auth.dto.UserInfo;
import com.runningRank.runningRank.auth.dto.UserUpdateRequest;
import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.emailVerification.service.EmailVerificationService;
import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.user.dto.ChangePasswordDTO;
import com.runningRank.runningRank.user.dto.PresignedUrlRequest;
import com.runningRank.runningRank.user.dto.PresignedUrlResponse;
import com.runningRank.runningRank.user.dto.UserVerification;
import com.runningRank.runningRank.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;


    @GetMapping("/verifications")
    public ResponseEntity<ApiResponse<List<UserVerification>>> getUserVerifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(ApiResponse.<List<UserVerification>>builder()
                .status(HttpStatus.OK.value())
                .message("유저 인증 기록 조회 성공")
                .data(userService.getUserVerifications(userId))
                .build());
    }
    /**
     * 사용자 정보를 수정하는 API 엔드포인트.
     * PUT /api/users/update-user-info
     *
     * @param request UserUpdateRequest DTO (수정할 사용자 정보)
     * @return 수정 성공 여부를 나타내는 ResponseEntity<ApiResponse<Boolean>>
     */
    @PutMapping("/update-user-info")
    public ResponseEntity<ApiResponse<UserInfo>> updateUserInfo(
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();

        return ResponseEntity.ok(ApiResponse.<UserInfo>builder()
                .status(HttpStatus.OK.value())
                .message("유저 정보 업데이트 완료")
                .data(userService.updateUserInfo(request, userId))
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

    /**
     * 아이디 인증용 이메일 보내기
     */
    @GetMapping("/sendMail")
    public ResponseEntity<ApiResponse<Boolean>> requestSendVerifyEmail(@RequestParam("univEmail") String univEmail){
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .status(HttpStatus.OK.value()) // 200
                        .message("학교 이메일 인증 요청")
                        .data(emailVerificationService.sendVerificationCode(univEmail))
                        .build());
    }

    /**
     * 이메일 인증 코드 검증하기
     */
    @GetMapping("/verifyCode")
    public ResponseEntity<ApiResponse<Boolean>> requestVerifyCode(
            @RequestParam("univEmail") String univEmail,
            @RequestParam("verifyCode") String verifyCode){
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .status(HttpStatus.OK.value()) // 200
                        .message("비번찾기 이메일 코드 검증")
                        .data(userService.verifyCode(univEmail,verifyCode))
                        .build());
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<ApiResponse<Boolean>> requestChangePassword(
            @RequestBody ChangePasswordDTO req){
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .status(HttpStatus.OK.value()) // 200
                        .message("비번찾기 이메일 코드 검증")
                        .data(userService.changeUserPassword(req.getEmail(),req.getNewPassword()))
                        .build());
    }

}
