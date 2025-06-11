package com.runningRank.runningRank.runningRecord.dto;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchoolTopRankDto {
    private int rank;
    private RunningType type;
    private int recordTimeInSeconds;
    private LocalDateTime recordDate;
    private SimpleUserDto user;
}
