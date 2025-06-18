package com.runningRank.runningRank.auth.dto;

import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RunningRecordDto {
    private int recordTimeInSeconds;
    private String marathonName;
    private String runningType; // 예: "HALF", "FULL"

    public static RunningRecordDto from(RunningRecord record) {
        return RunningRecordDto.builder()
                .recordTimeInSeconds(record.getRecordTimeInSeconds())
                .marathonName(record.getMarathonName())
                .runningType(String.valueOf(record.getRunningType().name())) // enum -> String
                .build();
    }
}
