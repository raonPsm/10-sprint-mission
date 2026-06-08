package com.sprint.mission.discodeit.dto.userstatus;


import java.time.Instant;
import java.time.LocalDateTime;

public record UserStatusUpdateRequest (
        Instant newLastActiveAt
) {}
