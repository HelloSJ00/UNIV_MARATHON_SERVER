package com.runningRank.runningRank.major.service;

import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.major.repository.MajorRepository;
import com.runningRank.runningRank.user.domain.School;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;

    public List<String> getMajorsBySchool(String schoolName) {
        School schoolEnum = School.valueOf(schoolName); // 예외 처리는 따로 필요
        List<Major> majors = majorRepository.findBySchool(schoolEnum);
        return majors.stream()
                .map(Major::getName)
                .toList();
    }
}
