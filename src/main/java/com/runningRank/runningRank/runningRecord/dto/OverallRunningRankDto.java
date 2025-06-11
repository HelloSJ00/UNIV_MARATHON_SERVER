package com.runningRank.runningRank.runningRecord.dto;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverallRunningRankDto {
    private int rank;
    private RunningType type;
    private int recordTimeInSeconds;
    private LocalDateTime recordDate;
    private SimpleUserDto user;
}
