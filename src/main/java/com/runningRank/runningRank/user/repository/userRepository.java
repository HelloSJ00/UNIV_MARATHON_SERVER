package com.runningRank.runningRank.user.repository;

import com.runningRank.runningRank.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface userRepository extends JpaRepository<User,Long> {
}
