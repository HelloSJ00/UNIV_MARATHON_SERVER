package com.runningRank.runningRank.user.repository;

import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.dto.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByOauthIdAndOauthProvider(String oauthId, String oauthProvider);
    Optional<User> findByStravaId(String stravaId);

    @Query(
            value = "SELECT user_id AS userId, " +
                    "marathon_name AS marathonName, " +
                    "running_type AS runningType, " +
                    "image_url AS imageUrl, " +         // ✅ imageURL → imageUrl
                    "record_time AS recordTime, " +     // ✅ recordTime 정확히 매칭
                    "status AS status, " +
                    "created_at AS createdAt " +
                    "FROM record_verification " +
                    "WHERE user_id = :userId ",
            nativeQuery = true
    )
    List<UserVerification> findGroupedVerification(@Param("userId") Long userId);
}
