package com.runningRank.runningRank.runningRecord.Controller;

import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.service.RunningRecordService;
import com.runningRank.runningRank.user.domain.School;
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
     * 학교 통합 전체 랭킹 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RunningRecord>>> getRanking(
            @RequestParam("type") RunningType type
    ) {
        List<RunningRecord> ranking = runningRecordService.getTop100RankingByType(type);

        return ResponseEntity.ok(
                ApiResponse.<List<RunningRecord>>builder()
                        .status(HttpStatus.OK.value())
                        .message("통합 러닝 랭킹 조회 성공")
                        .data(ranking)
                        .build()
        );
    }


    /**
     * 특정 학교 랭킹 조회
     * @param school
     * @param type
     * @return
     */
    @GetMapping("/univRankings")
    public ResponseEntity<ApiResponse<List<RunningRecord>>> getRanking(
            @RequestParam("school") School school,
            @RequestParam("type") RunningType type
    ) {
        List<RunningRecord> ranking = runningRecordService.getRankingsBySchoolAndType(school, type);
        return ResponseEntity.ok(
                ApiResponse.<List<RunningRecord>>builder()
                        .status(HttpStatus.OK.value())
                        .message("학교별 러닝 랭킹 조회 성공")
                        .data(ranking)
                        .build()
        );
    }
}
