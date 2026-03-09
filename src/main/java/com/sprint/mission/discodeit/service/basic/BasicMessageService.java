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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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

        // 첨부파일 엔티티 생성
        List<BinaryContent> attachmentEntities = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            for(BinaryContentCreateRequest fileReq : attachments) {
                attachmentEntities.add(new BinaryContent(fileReq.fileName(), (long) fileReq.bytes().length, fileReq.contentType()));
            }
        }

        // Message 엔티티 생성
        Message message = new Message(request.content(), channel, author, attachmentEntities);
        Message savedMessage = messageRepository.save(message);

        if (attachments != null && !attachments.isEmpty()) {
            for (int i = 0; i < attachments.size(); i++) {
                BinaryContentCreateRequest file = attachments.get(i);
                BinaryContent savedAttachment = savedMessage.getAttachments().get(i);
                binaryContentStorage.put(savedAttachment.getId(), file.bytes());
            }
        }

        // 영속성 전이
        return messageMapper.toDto(savedMessage);
    }

    @Transactional(readOnly = true)
    @Override
    public MessageDto findById(UUID messageId) {
        return messageMapper.toDto(messageRepository.findById(messageId)
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
        if(!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("채널이 존재하지 않습니다. id: " + channelId);
        }

        // 50개씩, 최근 생성일자(createdAt) 기준 내림차순 정렬 조건의 Pageable 생성
        Pageable customPageable = PageRequest.of(
                pageable.getPageNumber(),
                50,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Slice<Message> messageSlice = messageRepository.findAllByChannel_IdOrderByCreatedAtDesc(channelId, pageable);

        Slice<MessageDto> dtoSlice = messageSlice.map(messageMapper::toDto);

        return pageResponseMapper.fromSlice(dtoSlice, pageable.getPageNumber());

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
