package com.runningRank.runningRank.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private int status;       // HTTP 상태 코드
    private String message;   // 응답 메시지
    private T data;           // 실제 응답 데이터
}
