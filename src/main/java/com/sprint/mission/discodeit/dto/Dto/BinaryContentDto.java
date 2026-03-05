package com.sprint.mission.discodeit.dto.Dto;

import java.util.UUID;

public record BinaryContentDto (
        UUID id,
        String filename,
        Long size,
        String contentType,
        byte[] bytes
) {}