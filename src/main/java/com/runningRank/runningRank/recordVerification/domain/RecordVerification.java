package com.runningRank.runningRank.recordVerification.domain;

import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.user.domain.User;
import jakarta.persistence.*;

@Entity
public class RecordVerification {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String imageUrl;
    private String marathonName;

    @Enumerated(EnumType.STRING)
    private RunningType runningType;

    private int recordTime; // 초 단위

    @Enumerated(EnumType.STRING)
    private VerificationStatus status; // PENDING, APPROVED, REJECTED
}
