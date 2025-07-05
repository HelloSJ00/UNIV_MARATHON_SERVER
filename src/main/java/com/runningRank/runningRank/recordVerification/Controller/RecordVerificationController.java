package com.runningRank.runningRank.recordVerification.Controller;

import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.recordVerification.dto.GptCallbackRequest;
import com.runningRank.runningRank.recordVerification.dto.OcrCallbackRequest;
import com.runningRank.runningRank.recordVerification.service.RecordVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/recordVerification")
@RequiredArgsConstructor
public class RecordVerificationController {

    private final RecordVerificationService recordVerificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> verifyRecord(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        Long userId = userDetails.getId();
        String s3ImageUrl = body.get("s3ImageUrl");

        // ✅ 비동기로 Job 등록 및 OCR Lambda 트리거 (SQS 메시지 전송)
        UUID jobId = recordVerificationService.createRecordVerification(userId, s3ImageUrl);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("기록 검증 요청이 성공적으로 접수되었습니다. 결과는 추후 조회하세요.")
                .data(jobId.toString()) // 🔁 클라이언트가 결과를 추적할 수 있도록 jobId 반환
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback/ocr")
    public ResponseEntity<?> ocrCallback(@RequestBody OcrCallbackRequest request) {
        recordVerificationService.handleOcrCallback(
                UUID.fromString(request.getJobId()),
                request.getOcrResultS3Key()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/gpt")
    public ResponseEntity<?> gptCallback(@RequestBody GptCallbackRequest request) {
        recordVerificationService.handleGptCallback(
                UUID.fromString(request.getJobId()),
                request.getGptResultS3Key()
        );
        return ResponseEntity.ok().build();
    }
}
}
