package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.Set;
import java.util.UUID;

public record ChannelCreateRequest(
        ChannelType type, // (필수)
        String name, // (Public일 때 필수)
        String description, // (Public일 때 선택)
        Set<UUID> participantIds // (Private일 때 필수)
) {
}
