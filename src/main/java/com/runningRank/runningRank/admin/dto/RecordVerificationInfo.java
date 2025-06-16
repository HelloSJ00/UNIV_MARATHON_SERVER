package com.runningRank.runningRank.admin.dto;

import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RecordVerificationInfo {
    private Long userId;
    private Long recordVerificationId;
    private String imageUrl;
    private String marathonName;
    private RunningType runningType;
    private int recordTime; // 초 단위
    private VerificationStatus status;
}
