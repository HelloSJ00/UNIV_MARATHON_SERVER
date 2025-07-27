package com.runningRank.runningRank.strava.controller;

import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.strava.dto.StravaCallbackRequest;
import com.runningRank.runningRank.strava.service.StravaAuthService;
import com.runningRank.runningRank.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/strava")
@RequiredArgsConstructor
public class StravaAuthController {

    private final StravaAuthService stravaAuthService;

    /**
     * Strava 인증 시작 엔드포인트.
     * 사용자가 이 URL (예: http://localhost:8080/auth/strava/connect)에 접속하면
     * Strava 인증 페이지로 리다이렉션됩니다.
     *
     * @param userDetails 현재 로그인한 사용자의 정보 (스프링 시큐리티 컨텍스트에서 주입됨)
     * @return Strava 인증 페이지로의 리다이렉션 뷰
     */
    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> stravaConnect(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getId();
        String redirectUrl = stravaAuthService.generateStravaAuthUrl(currentUserId);

        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
    }


    /**
     * 콜백
     * @param code
     * @param state
     * @return
     */
    @GetMapping("/callback")
    public RedirectView stravaCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {
        try {
            // code와 state를 사용해서 토큰 교환 및 DB 업데이트
            stravaAuthService.exchangeCodeForTokens(code, state);

            // 성공 시 프론트엔드 마이페이지로 리다이렉트
            return new RedirectView("http://localhost:3000/mypage?strava=success");
        } catch (Exception e) {
            // 실패 시 에러 페이지 또는 fallback 페이지로 리다이렉트
            return new RedirectView("http://localhost:3000/mypage?strava=fail");
        }
    }
    /**
     * 3. 프론트엔드가 Strava 콜백에서 받은 code와 state를 가지고 호출하는 API.
     * 이 엔드포인트에서 최종적인 토큰 교환 및 사용자 정보 업데이트가 이루어집니다.
     * 이 메서드는 이전과 동일하게 JSON 응답을 반환합니다.
     *
     * @param request 프론트엔드에서 보낸 code와 state를 포함하는 DTO
     * @return 인증 성공/실패 여부와 앱 JWT 토큰을 포함하는 JSON 응답
     */
    @PostMapping("/exchange-token")
    public ResponseEntity<ApiResponse<User>> stravaCallback(@RequestBody StravaCallbackRequest request) {
        User updatedUser = stravaAuthService.exchangeCodeForTokens(
                request.getCode(),
                request.getState()
        );

        return ResponseEntity.ok(ApiResponse.<User>builder()
                .status(HttpStatus.OK.value())
                .message("Strava 인증 성공")
                .data(updatedUser)
                .build());
    }
}
