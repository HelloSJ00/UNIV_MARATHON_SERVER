package com.runningRank.runningRank.mileage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MileageRankingResponse {
    private List<MileageUnivRankDto> rankings;
    private MyMileageRankInfo myrecord;
}
