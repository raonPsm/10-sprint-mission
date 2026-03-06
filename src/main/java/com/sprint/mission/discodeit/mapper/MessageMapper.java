package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.MessageDto;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageMapper {
    private final BinaryContentMapper binaryContentMapper;
    private final UserMapper userMapper;

    public MessageDto toDto(Message message) {
        if (message == null) return null;

        return new MessageDto(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getChannel().getId(),
                userMapper.toDto(message.getAuthor()),
                binaryContentMapper.toListDto(message.getAttachments())
        );
    }

    public List<MessageDto> toListDto(List<Message> messages) {
        if (messages == null || messages.isEmpty()) return Collections.emptyList();

        return messages.stream()
                .map(this::toDto)
                .toList();
    }

    public MessageResponse toResponse(Message message) {
        if (message == null) return null;

        List<UUID> attachmentIds = message.getAttachments() == null
                ? Collections.emptyList()
                : message.getAttachments().stream().map(BinaryContent::getId).toList();

        return new MessageResponse(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getChannel().getId(),
                message.getAuthor().getId(),
                message.getContent(),
                attachmentIds
        );
    }
}
