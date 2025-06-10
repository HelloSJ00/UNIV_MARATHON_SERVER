package com.runningRank.runningRank.major.repository;

import com.runningRank.runningRank.major.domain.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major,Long> {
    Optional<Major> findByName(String majorName);
}
