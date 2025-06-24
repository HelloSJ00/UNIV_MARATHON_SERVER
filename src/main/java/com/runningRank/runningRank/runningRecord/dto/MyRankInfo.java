package com.runningRank.runningRank.runningRecord.dto;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor // 이 경우 세 필드를 모두 포함하는 생성자를 자동으로 만들어 줍니다.
public class MyRankInfo {
    private int recordTimeInSeconds; // 이 필드는 int로 유지해도 무방 (시간이 int 범위를 초과할 일은 거의 없음)
    private long ranking;             // int -> long 변경 (RANK() 결과는 BIGINT일 수 있음)
    private long totalCount;          // int -> long 변경 (COUNT() 결과는 BIGINT일 수 있음)
    private String gender;
    private String type; // rr.running_type;
    private String graduationStatus;
}
