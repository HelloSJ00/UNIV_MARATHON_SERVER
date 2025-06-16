package com.runningRank.runningRank.admin.controller;

import com.runningRank.runningRank.admin.dto.RecordConfirmRequest;
import com.runningRank.runningRank.admin.dto.RejectRequest;
import com.runningRank.runningRank.admin.service.AdminService;
import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.recordVerification.domain.RecordVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/record-verifications")
    public ResponseEntity<Page<RecordVerification>> getAllVerifications(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.getPendingRecordVerifications(pageable));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Boolean>> confirmRecordVerification(
            @RequestBody RecordConfirmRequest recordConfirmRequest
    ) {
        boolean result = adminService.confirmRecordVerification(
                recordConfirmRequest.getUserId(),
                recordConfirmRequest.getRecordVerificationId()
        );

        if (result) {
            return ResponseEntity.ok(
                    ApiResponse.<Boolean>builder()
                            .status(200)
                            .message("기록 검증이 승인되었습니다.")
                            .data(true)
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Boolean>builder()
                            .status(400)
                            .message("기록 검증 승인에 실패했습니다.")
                            .data(false)
                            .build()
            );
        }
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<Boolean>> rejectRecordVerification(
            @RequestBody RejectRequest rejectRequest
    ) {
        boolean result = adminService.rejectRecordVerification(rejectRequest.getRecordVerificationId());

        if (result) {
            return ResponseEntity.ok(
                    ApiResponse.<Boolean>builder()
                            .status(200)
                            .message("기록 검증이 거절되었습니다.")
                            .data(true)
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Boolean>builder()
                            .status(400)
                            .message("기록 검증 거절에 실패했습니다.")
                            .data(false)
                            .build()
            );
        }
    }
}
