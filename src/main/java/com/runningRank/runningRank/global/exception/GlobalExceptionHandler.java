package com.runningRank.runningRank.global.exception;

import com.runningRank.runningRank.global.dto.ApiResponse;
import com.runningRank.runningRank.recordVerification.exception.CallQuotaExceededException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpHeaders; // HttpHeaders 임포트 추가

import java.util.Map;

/**
 * 스웨거 오류로 @Hidden 어노테이션 붙혀놈
 */
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        // 1. CORS 헤더를 추가할 HttpHeaders 객체 생성
        HttpHeaders headers = new HttpHeaders();
        // 2. Access-Control-Allow-Origin 헤더 추가 (클라이언트 Origin에 맞춰야 함)
        // 실제 클라이언트 Origin이 "http://localhost:3000"이므로 동일하게 설정
        headers.add("Access-Control-Allow-Origin", "http://localhost:3000");
        // 3. 허용할 메서드 추가 (필요에 따라)
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        // 4. 허용할 헤더 추가 (클라이언트가 보낼 수 있는 모든 헤더를 허용하려면 "*")
        headers.add("Access-Control-Allow-Headers", "*");
        // 5. 자격 증명(쿠키, Authorization 헤더)을 허용한다면 추가
        headers.add("Access-Control-Allow-Credentials", "true");


        return ResponseEntity
                .badRequest() // HttpStatus.BAD_REQUEST (400)
                .headers(headers) // 생성한 CORS 헤더를 응답에 추가
                .body(ApiResponse.<Void>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(CallQuotaExceededException.class)
    public ResponseEntity<?> handleCallQuotaExceeded(CallQuotaExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS) // 429
                .body(Map.of(
                        "error", "quota_exceeded",
                        "message", e.getMessage()
                ));
    }


}