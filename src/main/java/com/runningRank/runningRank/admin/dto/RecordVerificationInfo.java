package com.runningRank.runningRank.admin.dto;

import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class RecordVerificationInfo {
    Long userId;
    private String imageUrl;
    private String marathonName;

    @Enumerated(EnumType.STRING)
    private RunningType runningType;

    private int recordTime; // 초 단위
    @Enumerated(EnumType.STRING)
    private VerificationStatus status; // PENDING, APPROVED, REJECTED
}
