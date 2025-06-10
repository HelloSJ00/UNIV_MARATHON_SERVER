package com.runningRank.runningRank.badge.domain;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.user.domain.School;
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
public class Badge {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private RunningType type;

    @Enumerated(EnumType.STRING)
    private School school; // 전공이 소속된 학교

    @Enumerated(EnumType.STRING)
    private Rank rank;      // "GOLD", "SILVER", "BRONZE"

    private LocalDate awardedAt;
}

