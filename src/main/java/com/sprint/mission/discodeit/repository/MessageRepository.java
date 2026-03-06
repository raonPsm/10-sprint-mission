package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    void deleteAllByChannelId(UUID channelId);

    // SELECT * FROM message
    // WHERE channel_id = ?
    // ORDER BY created_at ASC;
    List<Message> findAllByChannelIdOrderByCreatedAtAsc(UUID channelId);
}
