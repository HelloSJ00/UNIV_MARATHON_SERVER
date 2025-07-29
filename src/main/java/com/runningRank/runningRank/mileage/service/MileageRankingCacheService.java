package com.runningRank.runningRank.mileage.service;

import com.runningRank.runningRank.mileage.repository.MileageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MileageRankingCacheService {

    private final MileageRepository mileageRepository;


}
