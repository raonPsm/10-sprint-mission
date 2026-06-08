package com.sprint.mission.discodeit.dto.requestRespose.message;

import java.util.UUID;

public record MessageCreateRequest (
        String content,
        UUID channelId,
        UUID authorId
        // List<BinaryContentRequest> attachments
            // MultipartFile로 별도로 받으므로 제거
) {}
