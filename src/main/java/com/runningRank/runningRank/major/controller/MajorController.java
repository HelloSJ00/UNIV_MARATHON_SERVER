package com.runningRank.runningRank.major.controller;

import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.major.dto.SchoolResponse;
import com.runningRank.runningRank.major.service.MajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/major")
@RequiredArgsConstructor
@Slf4j
public class MajorController {

    private final MajorService majorService;

    /**
     * 학교별 모든 전공 조회
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getMajorsByUniversityName(@RequestParam String universityName) {
        try {
            List<String> majors = majorService.getMajorsByUniversityName(universityName);
            return ResponseEntity.ok(
                    ApiResponse.<List<String>>builder()
                            .status(HttpStatus.OK.value())
                            .message("전공 조회 성공")
                            .data(majors)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.<List<String>>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("존재하지 않는 학교입니다.")
                            .data(null)
                            .build()
                    );
        }
    }
}