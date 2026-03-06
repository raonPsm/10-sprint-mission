package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {
    List<ReadStatus> findAllByUserId(UUID userId);

    void deleteAllByChannelId(UUID channelId);

    // SELECT 1
    // FROM read_statuses
    // WHERE user_id = ? AND channel_id = ?
    // LIMIT 1;
    boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);
}
