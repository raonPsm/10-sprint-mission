package com.sprint.mission.discodeit.dto.requestRespose.channel;

import java.util.Set;
import java.util.UUID;

public record PrivateChannelCreateRequest (
        Set<UUID> participantIds // 초대할 유저 id 목록
) {}