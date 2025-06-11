package com.runningRank.runningRank.major.service;

import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.major.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;

    public List<String> getMajorsByUniversityName(String universityName) {
        List<Major> majors = majorRepository.findByUniversityName(universityName);
        return majors.stream()
                .map(Major::getName)
                .toList();
    }
}
