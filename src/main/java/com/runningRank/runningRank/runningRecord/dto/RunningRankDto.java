package com.runningRank.runningRank.runningRecord.dto;

public record RunningRankDto(
        String university,
        String type,
        String marathonName,
        Long userId,
        int time,
        int rank
) {}