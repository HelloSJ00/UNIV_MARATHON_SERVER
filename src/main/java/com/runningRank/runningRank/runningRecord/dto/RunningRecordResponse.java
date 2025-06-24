package com.runningRank.runningRank.runningRecord.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RunningRecordResponse {
    private List<OverallRunningRankDto> rankings;
    private MyRankInfo myrecord;
}
