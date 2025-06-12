package com.runningRank.runningRank.major.service;

import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.major.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MajorService {

    private final MajorRepository majorRepository;

    public List<String> getMajorsByUniversityName(String universityName) {
        List<Major> majors = majorRepository.findByUniversityName(universityName);
        majors.forEach(m -> System.out.println(m.getUniversityName() + " - " + m.getName()));
        return majors.stream()
                .map(Major::getName)
                .toList();
    }
}
