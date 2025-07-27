package com.runningRank.runningRank.mileage.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class MileageUpdateResponse {
    private Long userId; // 스프링 앱 내부의 User ID (DB PK)
    private int year;
    private int month;
    private double totalDistanceKm;
}
