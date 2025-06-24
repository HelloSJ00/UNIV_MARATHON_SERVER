package com.runningRank.runningRank.runningRecord.Controller;

import com.runningRank.runningRank.auth.model.CustomUserDetails;
import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.RunningRecordResponse;
import com.runningRank.runningRank.runningRecord.service.RunningRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/runningRecord")
@Slf4j
public class RunningRecordController {

    private final RunningRecordService runningRecordService;

    /**
     * 학교별 또는 전체 러닝 랭킹 조회 (성별 필터 포함)
     */
    @GetMapping("/school-ranking")
    public ResponseEntity<ApiResponse<RunningRecordResponse>> getRunningRankings(
            @RequestParam("runningType") RunningType type,
            @RequestParam(value = "universityName", required = false) String universityName,
            @RequestParam(value = "gender") String gender,
            @RequestParam(value = "graduationStatus") String graduationStatus,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        log.info("유저 ID = " + userId);
        RunningRecordResponse response = runningRecordService.getTop100RankingsWithMyRecord(
                userId,
                universityName,
                type,
                gender,
                graduationStatus
        );

        return ResponseEntity.ok(
                ApiResponse.<RunningRecordResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("러닝 랭킹 조회 성공")
                        .data(response)
                        .build()
        );
    }

}
