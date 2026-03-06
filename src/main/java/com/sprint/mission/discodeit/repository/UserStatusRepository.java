package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository extends JpaRepository<UserStatus, UUID> {
    Optional<UserStatus> findByUser_Id(UUID userId);  // 유저 id로 유저 상태 조회
    void deleteByUser_Id(UUID userId);
    boolean existsByUser_Id(UUID userId);

    <T> Optional<T> findByUserId(UUID id);
}
