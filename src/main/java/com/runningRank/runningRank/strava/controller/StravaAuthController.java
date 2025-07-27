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

@RestController
@RequestMapping("/auth/strava")
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
    public RedirectView stravaConnect(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getId(); // 실제 로그인된 사용자의 고유 ID로 대체하세요.
        return new RedirectView(stravaAuthService.generateStravaAuthUrl(currentUserId));
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
