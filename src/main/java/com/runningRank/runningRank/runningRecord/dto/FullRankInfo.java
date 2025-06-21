package com.runningRank.runningRank.runningRecord.dto;

import com.runningRank.runningRank.runningRecord.domain.RunningType;

import java.time.LocalDateTime;

public interface FullRankInfo {
    Long getUserId();
    String getUserName();
    String getUserGender();
    String getUniversityName();
    RunningType getRunningType(); // Enum도 가능
    String getMarathonName();
    Integer getRecordTimeInSeconds();
    LocalDateTime getCreatedAt();
    Integer getRanking();
    Integer getTotalCount();
}

