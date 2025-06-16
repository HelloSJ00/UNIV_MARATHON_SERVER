package com.runningRank.runningRank.recordVerification.Controller;

import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.recordVerification.service.RecordVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recordVerification")
@RequiredArgsConstructor
public class RecordVerificationController {

    private final RecordVerificationService recordVerificationService;

    @PostMapping
    public ResponseEntity<String> verifyRecord(@RequestBody Map<String, String> body) {
        // 🔐 현재 로그인한 유저의 ID를 SecurityContext에서 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        recordVerificationService.createRecordVerification(userId,body.get("s3ImageUrl"));
        return ResponseEntity.ok("기록 검증 요청이 성공적으로 처리되었습니다.");
    }
}
