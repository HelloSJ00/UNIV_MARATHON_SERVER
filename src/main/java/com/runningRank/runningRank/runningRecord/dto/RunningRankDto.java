package com.runningRank.runningRank.runningRecord.dto;

public record RunningRankDto(
        String university,
        String type,
        Long userId,
        int time,
        int rank
) {}