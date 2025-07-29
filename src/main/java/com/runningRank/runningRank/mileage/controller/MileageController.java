package com.runningRank.runningRank.mileage.controller;

import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.mileage.dto.MileageRankingResponse;
import com.runningRank.runningRank.mileage.dto.MileageUpdateResponse;
import com.runningRank.runningRank.mileage.service.MileageQueueSendService;
import com.runningRank.runningRank.mileage.service.MileageRankingService;
import com.runningRank.runningRank.mileage.service.MileageService;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.RunningRecordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mileage")
@RequiredArgsConstructor
public class MileageController {

    private final MileageService mileageService;
    private final MileageRankingService mileageRankingService;
    private final MileageQueueSendService mileageQueueSendService;

    @GetMapping
    public String getMileage() {
        mileageQueueSendService.sendMileageToQueue();
        return "success";
    }

    @PostMapping("/callback-mileage")
    public String callbackMileage(@RequestBody MileageUpdateResponse mileageUpdateResponse) {
        mileageService.saveOrUpdateMonthlyMileage(mileageUpdateResponse);
        return "success";
    }

    /**
     * 학교별 또는 전체 러닝 랭킹 조회 (성별 필터 포함)
     */
    @GetMapping("/mileage-ranking")
    public ResponseEntity<ApiResponse<MileageRankingResponse>> getMileageRankings(
            @RequestParam(value = "universityName", required = false) String universityName,
            @RequestParam(value = "gender") String gender,
            @RequestParam(value = "graduationStatus") String graduationStatus,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        MileageRankingResponse response = mileageRankingService.getTop100MileageRankingsWithMyRecord(
                userId,
                universityName,
                gender,
                graduationStatus
        );

        return ResponseEntity.ok(
                ApiResponse.<MileageRankingResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("러닝 랭킹 조회 성공")
                        .data(response)
                        .build()
        );
    }
}
