package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {
    List<ReadStatus> findAllByUserId(UUID userId);

    // Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId);
    // TODO: 특정 채널의 메시지를 특정 유저가 읽었는지 확인할 때 필요할 수 있음 -> 비즈니스 로직 고려 후 판단
}
