package com.runningRank.runningRank.major.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SchoolResponse {
    private String enumCode;     // 예: "SCHOOL_001"
    private String displayName;  // 예: "가톨릭대학교"
}
