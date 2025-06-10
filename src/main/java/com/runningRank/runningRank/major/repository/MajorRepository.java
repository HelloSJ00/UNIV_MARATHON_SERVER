package com.runningRank.runningRank.major.repository;

import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.user.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major,Long> {
    Optional<Major> findByName(String majorName);
    List<Major> findBySchool(School school);
}
