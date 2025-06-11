package com.runningRank.runningRank.university.repository;

import com.runningRank.runningRank.university.domain.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University,Long> {
    Optional<University> findByUniversityName(String universityName);
}
