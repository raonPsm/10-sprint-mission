package com.sprint.mission.discodeit.dto.requestRespose.userstatus;


import java.time.Instant;

public record UserStatusUpdateRequest(
    Instant newLastActiveAt
) {

}
