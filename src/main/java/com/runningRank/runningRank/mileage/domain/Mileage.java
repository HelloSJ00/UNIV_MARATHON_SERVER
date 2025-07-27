package com.runningRank.runningRank.mileage.domain;

import com.runningRank.runningRank.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mileage", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "year", "month"}) // 복합 유니크 키
})
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자 자동 생성, 접근 제한
@AllArgsConstructor
@Builder // 빌더 패턴 자동 생성
public class Mileage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int year;

    private int month;

    private double totalDistanceKm;

    private LocalDateTime lastUpdatedAt;

    // -------------------------------------------------------------
    // 엔티티 내부에 빌더 패턴을 활용한 정적 팩토리 메서드 추가
    // -------------------------------------------------------------
    public static Mileage of(User user, int year, int month, double totalDistanceKm) {
        return Mileage.builder()
                .user(user)
                .year(year)
                .month(month)
                .totalDistanceKm(totalDistanceKm)
                .lastUpdatedAt(LocalDateTime.now()) // 객체 생성 시 현재 시간으로 설정
                .build();
    }

    public void updateTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
