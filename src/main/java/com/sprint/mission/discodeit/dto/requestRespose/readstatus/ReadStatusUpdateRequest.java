package com.sprint.mission.discodeit.dto.requestRespose.readstatus;

import java.time.Instant;

public record ReadStatusUpdateRequest(
    Instant newLastReadAt
) {

}