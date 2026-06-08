package com.sprint.mission.discodeit.dto.requestRespose.channel;

public record PublicChannelUpdateRequest(
        String newName,
        String newDescription
) {}