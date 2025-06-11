package com.runningRank.runningRank.badge.domain;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "badge")  // 명시적으로 추가해주는 걸 추천
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private RunningType type;

    private String unversity; // 전공이 소속된 학교

    @Enumerated(EnumType.STRING)
    private RunningRank runningRank;      // "GOLD", "SILVER", "BRONZE"

    private LocalDate awardedAt;
}

