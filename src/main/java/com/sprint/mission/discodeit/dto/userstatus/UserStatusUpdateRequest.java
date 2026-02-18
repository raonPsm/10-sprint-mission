package com.sprint.mission.discodeit.dto.userstatus;


import java.time.LocalDateTime;

public record UserStatusUpdateRequest (
        LocalDateTime newLastActiveAt
) {}
