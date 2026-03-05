package com.sprint.mission.discodeit.dto.binarycontent;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentResponse(
        UUID id,
        Instant createdAt,
        String fileName,
        String contentType,
        long size,
        byte[] bytes
) {}

