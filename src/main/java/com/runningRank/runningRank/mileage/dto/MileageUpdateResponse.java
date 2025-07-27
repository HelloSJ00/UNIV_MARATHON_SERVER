package com.runningRank.runningRank.mileage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MileageUpdateResponse {
    private Long userId; // 스프링 앱 내부의 User ID (DB PK)
    private int year;
    private int month;
    private int totalActivityCount;
    private double totalDistanceKm;
    private int avgPaceTime;
}
