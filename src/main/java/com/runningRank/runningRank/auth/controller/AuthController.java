package com.runningRank.runningRank.auth.controller;


import com.runningRank.runningRank.auth.dto.LoginRequest;
import com.runningRank.runningRank.auth.dto.SignUpRequest;
import com.runningRank.runningRank.auth.dto.TokenResponse;
import com.runningRank.runningRank.auth.dto.UserResponse;
import com.runningRank.runningRank.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}
