package com.runningRank.runningRank.recordVerification.Controller;

import com.runningRank.runningRank.recordVerification.dto.GptCallbackRequest;
import com.runningRank.runningRank.recordVerification.dto.OcrCallbackRequest;
import com.runningRank.runningRank.recordVerification.service.RecordVerificationCallbackService;
import com.runningRank.runningRank.recordVerification.service.RecordVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/recordVerification/callback")
@RequiredArgsConstructor
public class RecordVerificationCallBackController {

    private final RecordVerificationCallbackService recordVerificationCallbackService;

    @PostMapping("/ocr")
    public ResponseEntity<?> ocrCallback(@RequestBody OcrCallbackRequest request) {
        recordVerificationCallbackService.handleOcrCallback(
                request.getUserId(),
                UUID.fromString(request.getJobId()),
                request.getS3ImageUrl(),
                request.getOcrResultS3Key()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/gpt")
    public ResponseEntity<?> gptCallback(@RequestBody GptCallbackRequest request) {
        recordVerificationCallbackService.handleGptCallback(
                request.getUserId(),
                UUID.fromString(request.getJobId()),
                request.getS3ImageUrl(),
                request.getGptResultS3Key()
        );
        return ResponseEntity.ok().build();
    }
}
