package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.requestRespose.message.MessageResponse;
import com.sprint.mission.discodeit.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public MessageResponse toResponse(Message message) {
        if (message == null) return null;
        return new MessageResponse(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getChannelId(),
                message.getAuthorId(),
                message.getContent(),
                message.getAttachmentIds()
        );
    }

}
