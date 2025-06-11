package com.runningRank.runningRank.badge.repository;

import com.runningRank.runningRank.badge.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge,Long> {



}
