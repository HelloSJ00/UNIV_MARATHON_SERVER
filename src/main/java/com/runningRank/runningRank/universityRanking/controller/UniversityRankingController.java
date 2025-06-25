package com.runningRank.runningRank.universityRanking.controller;

import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.runningRecord.dto.RunningRecordResponse;
import com.runningRank.runningRank.universityRanking.dto.FinisherUnivRanking;
import com.runningRank.runningRank.universityRanking.service.UniversityRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankings/universities") // 이 컨트롤러의 기본 URL 경로 설정
@RequiredArgsConstructor
public class UniversityRankingController {
    private final UniversityRankingService universityRankingService;

    /**
     * 종목 : runningType 에 따른 대학교 랭킹 목록을 조회합니다.
     * 예시 URL: /api/rankings/universities/finisher?runningType=FULL_MARATHON
     *
     * @param runningType 조회할 달리기 유형 (예: FULL_MARATHON, HALF_MARATHON)
     * @return FinisherUnivRanking DTO 리스트와 HTTP 200 OK 응답
     */
    @GetMapping("/finisher") // GET 요청에 대한 엔드포인트 설정
    public ResponseEntity<ApiResponse<List<FinisherUnivRanking>>> getFinisherUniversityRankings(
            @RequestParam("runningType") String runningType) { // 쿼리 파라미터로 runningType을 받음

        // 서비스 계층의 메서드를 호공하여 랭킹 데이터 가져오기
        List<FinisherUnivRanking> rankings = universityRankingService.getFinisherUniversityRankings(runningType);

        // HTTP 200 OK 상태 코드와 함께 DTO 리스트 반환
        return ResponseEntity.ok(ApiResponse.<List<FinisherUnivRanking>>builder()
                .status(HttpStatus.OK.value())
                .message("학교별 완주자수 랭킹 조회 성공")
                .data(rankings)
                .build());
    }

    // 필요하다면 다른 랭킹 조회 API 엔드포인트도 추가할 수 있습니다.
    // 예: @GetMapping("/total") (전체 대학 랭킹)
}
