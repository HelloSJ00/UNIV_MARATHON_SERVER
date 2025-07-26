package com.runningRank.runningRank.mileage.domain;

import com.runningRank.runningRank.user.domain.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mileage", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "year", "month"}) // 복합 유니크 키
})
public class Mileage {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int year;

    private int month;

    private double totalDistanceKm;

    private LocalDateTime lastUpdatedAt;
}
