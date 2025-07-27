package com.runningRank.runningRank.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component // 스프링 빈으로 등록
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationScheduler {

    private final CacheManager cacheManager; // 캐시 매니저 주입

    // 캐시 이름 -> 해당 캐시의 마지막 무효화 요청 시간
    private final Map<String, LocalDateTime> invalidationRequests = new ConcurrentHashMap<>();

    // 캐시 무효화를 지연시킬 시간 (10분)
    private static final long INVALIDATION_DELAY_MINUTES = 10;

    /**
     * 특정 캐시를 N분 뒤에 무효화하도록 요청을 등록합니다.
     * @param cacheName 무효화할 캐시 이름
     */
    public void requestCacheInvalidation(String cacheName) {
        log.info("[캐시 무효화 요청 등록] 캐시 '{}'를 {}분 뒤에 무효화하도록 요청합니다.", cacheName, INVALIDATION_DELAY_MINUTES);
        invalidationRequests.put(cacheName, LocalDateTime.now()); // 현재 시간으로 업데이트
    }

    /**
     * 주기적으로 실행되어 만료된 캐시 무효화 요청을 처리합니다.
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES) // 매 1분마다 실행
    public void processPendingCacheInvalidations() {
        if (invalidationRequests.isEmpty()) {
            // log.debug("[캐시 스케줄러] 처리할 캐시 무효화 요청이 없습니다.");
            return;
        }

        log.info("[캐시 스케줄러 실행] 보류 중인 캐시 무효화 요청 확인 중...");
        LocalDateTime now = LocalDateTime.now();

        invalidationRequests.forEach((cacheName, requestTime) -> {
            if (requestTime.plusMinutes(INVALIDATION_DELAY_MINUTES).isBefore(now)) {
                // 무효화 요청 시간이 지연 시간 + 현재 시간보다 이전이면 (즉, 무효화할 시간이 지났으면)
                log.info("[캐시 무효화 실행] 캐시 '{}'의 무효화 시간이 지났으므로 모든 항목을 삭제합니다.", cacheName);
                // 해당 캐시의 모든 항목을 무효화
                cacheManager.getCache(cacheName).clear();
                invalidationRequests.remove(cacheName); // 처리 후 맵에서 제거
            }
        });
    }
}
