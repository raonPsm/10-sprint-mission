package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    void deleteAllByChannel_Id(UUID channelId);

    // SELECT * FROM message
    // WHERE channel_id = ?
    // ORDER BY created_at ASC;
    // List<Message> findAllByChannel_IdOrderByCreatedAtAsc(UUID channelId);

    Slice<Message> findAllByChannel_IdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);
}
