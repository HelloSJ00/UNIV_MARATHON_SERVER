package com.runningRank.runningRank.recordVerification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RecordInfo {
    @JsonProperty("대회명")
    private String marathonName;

    @JsonProperty("종목")
    private String runningType;

    @JsonProperty("기록")
    private String record;

    // getter/setter
}