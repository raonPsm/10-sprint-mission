package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// GET /api/channels - User가 참여 중인 Channel 목록 조회에서만 사용
public record ChannelDto(
        UUID id,
        ChannelType type,
        String name,
        String description,
        List<UUID> participantIds,
        Instant lastMessageAt
) {
}