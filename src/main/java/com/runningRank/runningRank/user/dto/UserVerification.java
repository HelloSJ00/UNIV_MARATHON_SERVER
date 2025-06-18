package com.runningRank.runningRank.user.dto;


import java.time.LocalDateTime;

public interface UserVerification {
    String getImageUrl();
    String getRunningType();
    String getMarathonName();
    int getRecordTime();               // ✅ 오타 수정
    String getStatus();
    LocalDateTime getCreatedAt();
}
