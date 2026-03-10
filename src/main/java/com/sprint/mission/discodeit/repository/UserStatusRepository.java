package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository extends JpaRepository<UserStatus, UUID> {
    // UserStatus save(UserStatus entity);
    // Optional<UserStatus> findById(UUID id);
    // List<UserStatus> findAll();
    // void delete(UserStatus entity);

    // 유저 id로 유저 상태 조회
    // JPQL -> SELECT us FROM UserStatus us WHERE us.user_id = :userId
    // SQL  -> SELECT us.* FROM user_statuses us WHERE us.user_id = ?
    Optional<UserStatus> findByUser_Id(UUID userId);

    // JPQL -> SELECT COUNT(us) > 0 FROM UserStatus us WHERE us.user_id = :userId
    // SQP  -> SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM user_statuses WHERE user_id = ?
    boolean existsByUser_Id(UUID userId);
}
