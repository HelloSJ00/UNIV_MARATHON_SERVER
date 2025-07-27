package com.runningRank.runningRank.mileage.repository;

import com.runningRank.runningRank.mileage.domain.Mileage;
import com.runningRank.runningRank.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MileageRepository extends JpaRepository<Mileage, Long> {
    Optional<Mileage> findByUserAndYearAndMonth(User user, int year, int month);
}
