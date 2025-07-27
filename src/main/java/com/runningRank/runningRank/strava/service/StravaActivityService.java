package com.runningRank.runningRank.strava.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class StravaActivityService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
}
