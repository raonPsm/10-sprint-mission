package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Dto.MessageDto;
import com.sprint.mission.discodeit.dto.requestRespose.PageResponse;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;


// FIXME: 메서드 내부 DTO 사용 부분 entity로 수정 및 기타 부수효과 문제 수정
@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentRepository binaryContentRepository; // FIXME: binaryContentRepository 코드에서 구현 필요
    private final BinaryContentStorage binaryContentStorage;
    private final PageResponseMapper pageResponseMapper;

    @Transactional
    @Override
    public MessageDto create(MessageCreateRequest request, List<BinaryContentCreateRequest> attachments) {
        // TODO: getReferenceById -> 프록시 사용 가능 -> 고려
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NoSuchElementException("채널이 존재하지 않습니다. id: " + request.channelId()));
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new NoSuchElementException("작성자가 존재하지 않습니다. id: " + request.authorId()));

        // List<BinaryContent> attachments 생성
        List<BinaryContent> attachmentList = attachments.stream()
                .map(req -> {
                    BinaryContent binaryContent = new BinaryContent(
                            req.fileName(),
                            (long)req.bytes().length,
                            req.contentType()
                    );
                    binaryContentRepository.save(binaryContent);
                    binaryContentStorage.put(binaryContent.getId(), req.bytes());
                    return binaryContent;
                })
                .toList();

        // Message 생성
        Message message = new Message(
                request.content(),
                channel,
                author,
                attachmentList
        );

        // 영속성 전이
        return messageMapper.toDto(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    @Override
    public MessageDto find(UUID messageId) {
        return messageMapper.toDto(
                messageRepository.findById(messageId)
                        .orElseThrow(() -> new NoSuchElementException("해당 메시지가 존재하지 않습니다. id: " + messageId))
        );
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<MessageDto> findAllByChannelId(
            UUID channelId,
            Pageable pageable,
            Instant createdAt
    ) {
        Slice<MessageDto> slice = messageRepository.findAllByChannelIdWithAuthor(channelId,
                        Optional.ofNullable(createdAt).orElse(Instant.now()),
                        pageable)
                .map(messageMapper::toDto);

        String nextCursor = null;
        if (!slice.getContent().isEmpty()) {
            nextCursor = slice.getContent().get(slice.getContent().size() - 1)
                    .createdAt()
                    .toString();
        }

        return pageResponseMapper.fromSlice(slice, nextCursor);
    }

    // 사진은 update 불가능
    @Transactional
    @Override
    public MessageDto update(UUID messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("해당 메시지가 존재하지 않습니다. id: " + messageId));
        message.update(request.newContent());
        return messageMapper.toDto(message);
    }

    @Transactional
    @Override
    public void delete(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("해당 메시지가 존재하지 않습니다. id: " + messageId));

        // 영속성 전이 + 고아 객체 제거
        messageRepository.delete(message);
    }
}
