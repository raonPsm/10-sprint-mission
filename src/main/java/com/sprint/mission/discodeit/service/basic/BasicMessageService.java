package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentService binaryContentService;

    @Override
    public MessageResponse create(MessageCreateRequest request, List<BinaryContentRequest> attachments) {
        if (!channelRepository.existsById(request.channelId())) {
            throw new NoSuchElementException("채널이 존재하지 않습니다. id: " + request.channelId());
        }
        if (!userRepository.existsById(request.authorId())) {
            throw new NoSuchElementException("작성자가 존재하지 않습니다. id: " + request.authorId());
        }

        // 첨부파일 저장
        List<UUID> attachmentIds = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            for (BinaryContentRequest fileRequest : attachments) {
                BinaryContentResponse savedContent = binaryContentService.create(fileRequest);
                attachmentIds.add(savedContent.id());
            }
        }

        // 메시지 저장
        Message message = new Message(
                request.content(),
                request.channelId(),
                request.authorId(),
                attachmentIds
        );
        Message savedMessage = messageRepository.save(message);

        return toResponse(savedMessage);
    }


    @Override
    public MessageResponse findById(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("해당 메시지가 존재하지 않습니다. id: " + messageId));
        return toResponse(message);
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        if(!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("채널이 존재하지 않습니다. id: " + channelId);
        }

        // 해당 채널의 메시지만 필터링하여 조회
        return messageRepository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .sorted(Comparator.comparing(Message::getCreatedAt)) // 생성 시간 순 정렬
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 사진은 update 불가능
    @Override
    public MessageResponse update(UUID messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("해당 메시지가 존재하지 않습니다. id: " + messageId));

        message.update(request.content());
        Message updatedMessage = messageRepository.save(message);

        return toResponse(updatedMessage);
    }

    @Override
    public void delete(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("해당 메시지가 존재하지 않습니다. id: " + messageId));

        // 메시지 연관 첨부파일 삭제
        if (message.getAttachmentIds() != null) {
            for (UUID attachmentId : message.getAttachmentIds()) {
                binaryContentRepository.deleteById(attachmentId);
            }
        }

        // 메시지 삭제
        messageRepository.deleteById(messageId);
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getChannelId(),
                message.getAuthorId(),
                message.getAttachmentIds(),
                message.getCreatedAt()
        );
    }
}
