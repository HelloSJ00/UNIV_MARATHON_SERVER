package com.runningRank.runningRank.mileage.controller;

import com.runningRank.runningRank.mileage.dto.MileageUpdateResponse;
import com.runningRank.runningRank.mileage.service.MileageQueueSendService;
import com.runningRank.runningRank.mileage.service.MileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mileage")
@RequiredArgsConstructor
public class MileageController {

    private final MileageService mileageService;
    private final MileageQueueSendService mileageQueueSendService;

    @GetMapping
    public String getMileage() {
        mileageQueueSendService.sendMileageToQueue();
        return "success";
    }

    @PostMapping("/callback-mileage")
    public String callbackMileage(@RequestBody MileageUpdateResponse mileageUpdateResponse) {
        mileageService.saveOrUpdateMonthlyMileage(mileageUpdateResponse);
        return "success";
    }
}
