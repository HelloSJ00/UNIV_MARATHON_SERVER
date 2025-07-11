package com.runningRank.runningRank.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration // 설정 클래스임을 명시
@EnableCaching // 캐싱 기능 활성화
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // 1. 'allUniversitiesCache' 캐시 설정: 30분 동안 미접근 시 만료
        CaffeineCache allUniversitiesCache = new CaffeineCache("allUniversitiesCache",
                Caffeine.newBuilder()
                        .maximumSize(10) // 최대 100개 항목
                        .expireAfterAccess(24, TimeUnit.HOURS) // 24시간
                        .build());

        // 2. 'majorsByUniversityCache' 캐시 설정: 10분 동안 미접근 시 만료
        CaffeineCache majorsByUniversityCache = new CaffeineCache("majorsByUniversityCache",
                Caffeine.newBuilder()
                        .maximumSize(30) // 최대 500개 항목 (대학별 전공이 많을 수 있으니 더 크게)
                        .expireAfterAccess(24, TimeUnit.HOURS) // 24시간
                        .build());

        // 3. 'runningRankCache' 캐시 설정: 1분 미접근 시 만료, 30초 후 비동기 새로고침
        CaffeineCache top100RankingsCache = new CaffeineCache("top100RankingsCache",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterAccess(1, TimeUnit.MINUTES) // 1분
                        .refreshAfterWrite(30, TimeUnit.SECONDS) // 30초 후 비동기 새로고침
                        .build());

        // 더 많은 캐시를 추가할 수 있습니다.

        // 모든 캐시를 CacheManager에 등록
        cacheManager.setCaches(Arrays.asList(
                allUniversitiesCache,
                majorsByUniversityCache,
                top100RankingsCache
        ));
        return cacheManager;
}
}
