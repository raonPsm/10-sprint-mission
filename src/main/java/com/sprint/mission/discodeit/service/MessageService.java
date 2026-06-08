package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.Dto.MessageDto;
import com.sprint.mission.discodeit.dto.requestRespose.PageResponse;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageDto create(
            MessageCreateRequest request,
            List<BinaryContentCreateRequest> attachments
    );

    MessageDto find(UUID messageId);

    PageResponse<MessageDto> findAllByChannelId(
            UUID channelId, Instant createdAt, Pageable pageable
    );

    MessageDto update(UUID messageId, MessageUpdateRequest request);

    // 첨부파일 함께 삭제
    void delete(UUID messageId);
}
