package com.runningRank.runningRank.runningRecord.dto;

public record RunningRankDto(
        String school,
        String type,
        Long userId,
        int time,
        int rank
) {}