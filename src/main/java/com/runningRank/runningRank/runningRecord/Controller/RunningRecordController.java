package com.runningRank.runningRank.runningRecord.Controller;

import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.OverallRunningRankDto;
import com.runningRank.runningRank.runningRecord.service.RunningRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/runningRecord")
public class RunningRecordController {

    private final RunningRecordService runningRecordService;

    /**
     * 학교별 또는 전체 러닝 랭킹 조회 (성별 필터 포함)
     */
    @GetMapping("/school-ranking")
    public ResponseEntity<ApiResponse<List<OverallRunningRankDto>>> getRunningRankings(
            @RequestParam("runningType") RunningType type,
            @RequestParam(value = "universityName", required = false) String universityName,
            @RequestParam(value = "gender") String gender // ← 기본값 설정
    ) {
        List<OverallRunningRankDto> ranking;

        if (universityName == null || universityName.isBlank()) {
            // 전체 학교 랭킹
            if(gender.equals("ALL")){
                ranking = runningRecordService.getTopRankingsByType(type, "");
            } else {
                ranking = runningRecordService.getTopRankingsByType(type, gender);
            }

        } else {
            // 특정 학교 랭킹
            if(gender.equals("ALL")){
                ranking = runningRecordService.getRankingsBySchoolAndType(universityName, type, "");
            } else {
                ranking = runningRecordService.getRankingsBySchoolAndType(universityName, type, gender);
            }
        }

        return ResponseEntity.ok(
                ApiResponse.<List<OverallRunningRankDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("러닝 랭킹 조회 성공")
                        .data(ranking)
                        .build()
        );
    }
}
