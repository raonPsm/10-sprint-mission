package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    void deleteAllByChannel_Id(UUID channelId);

    // SELECT * FROM message
    // WHERE channel_id = ?
    // ORDER BY created_at ASC;
    // List<Message> findAllByChannel_IdOrderByCreatedAtAsc(UUID channelId);

    Slice<Message> findAllByChannel_IdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

    Slice<Message> findAllByChannel_IdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID channelId, Instant createdAt, Pageable pageable);

    @Query("SELECT m FROM Message m "
            + "LEFT JOIN FETCH m.author a "
            + "JOIN FETCH a.userStatus"
            + " JOIN FETCH a.profile "
            + "WHERE m.channel.id=:channelId AND m.createdAt < :createdAt")
    Slice<Message> findAllByChannelIdWithAuthor(@Param("channelId") UUID channelId,
                                                @Param("createdAt") Instant createdAt,
                                                Pageable pageable);
}
