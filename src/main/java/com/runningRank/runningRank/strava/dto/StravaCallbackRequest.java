package com.runningRank.runningRank.strava.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StravaCallbackRequest {
    private String code;
    private String state;
}
