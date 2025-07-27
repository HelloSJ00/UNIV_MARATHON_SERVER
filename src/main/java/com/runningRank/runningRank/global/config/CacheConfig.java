package com.runningRank.runningRank.global.config;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration // 설정 클래스임을 명시
@EnableCaching // 캐싱 기능 활성화
public class CacheConfig {

    // CaffeineCacheManager를 사용할 때 CacheLoader 빈을 정의하는 것이 일반적입니다.
    // refreshAfterWrite를 사용하지 않아도 Spring Cache AOP가 로딩을 처리하므로, 여기서는 더미 로더를 사용해도 됩니다.
    @Bean
    public CacheLoader<Object, Object> cacheLoader() {
        return new CacheLoader<Object, Object>() {
            @Override
            public Object load(Object key) {
                return null; // 실제 로딩은 @Cacheable 메소드에서 처리됨
            }
        };
    }

    @Bean
    public CacheManager cacheManager(CacheLoader<Object, Object> cacheLoader) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // CaffeineCacheManager에 CacheLoader 설정
        cacheManager.setCacheLoader(cacheLoader);

        // 각 캐시의 설정을 registerCustomCache를 통해 등록합니다.
        cacheManager.registerCustomCache("allUniversitiesCache",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterAccess(24, TimeUnit.HOURS)
                        .build());

        cacheManager.registerCustomCache("majorsByUniversityCache",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterAccess(24, TimeUnit.HOURS)
                        .build());

        cacheManager.registerCustomCache("top100RankingsCache",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterAccess(24, TimeUnit.HOURS)
                        .build());

        return cacheManager;
    }
}
