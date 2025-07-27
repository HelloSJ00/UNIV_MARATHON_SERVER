package com.runningRank.runningRank.strava.controller;

import com.runningRank.runningRank.strava.service.StravaActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StravaActivityController {

    private final StravaActivityService stravaActivityService;
}
