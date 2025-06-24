package com.runningRank.runningRank.badge.controller;

import com.runningRank.runningRank.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/badge")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ResponseEntity<String> test(){
//        badgeService.top3bySchoolAndByRunningType();
        return ResponseEntity.ok("ok ");
    }
}
