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
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// FIXME: 메서드 내부 DTO 사용 부분 entity로 수정 및 기타 부수효과 문제 수정
@RequiredArgsConstructor
@Service
@Slf4j
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
  public MessageDto create(MessageCreateRequest request,
      List<BinaryContentCreateRequest> attachments
  ) {
    log.info("[MESSAGE_CREATE] 메시지 생성 요청: channelId={}, authorId={}, attachmentCount={}",
        request.channelId(), request.authorId(), attachments.size()
    );

    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(Map.of("channelId", request.channelId())));
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new UserNotFoundException(Map.of("authorId", request.authorId())));

    // List<BinaryContent> attachments 생성
    List<BinaryContent> attachmentList = attachments.stream()
        .map(req -> {
          BinaryContent binaryContent = new BinaryContent(
              req.fileName(),
              (long) req.bytes().length,
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
    MessageDto result = messageMapper.toDto(messageRepository.save(message));
    log.info("[MESSAGE_CREATE] 메시지 생성 완료: messageId={}, channelId={}, authorId={}",
        result.id(), request.channelId(), request.authorId()
    );
    return result;
  }

  @Transactional(readOnly = true)
  @Override
  public MessageDto find(UUID messageId) {
    log.debug("[MESSAGE_FIND] 메시지 조회 요청: messageId={}", messageId);
    return messageMapper.toDto(
        messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException(Map.of("messageId", messageId)))
    );
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<MessageDto> findAllByChannelId(
      UUID channelId, // 메시지를 조회할 특정 채널의 식별자
      Instant createdAt, // 페이징의 기준점이 되는 cursor값. 이 시간보다 이전에 생성된 데이터를 찾는 용도로 사용
      Pageable pageable // 페이지의 크기, 정렬 등의 페이징 정보를 담고 있음

  ) {
    log.debug("[MESSAGE_FIND_BY_CHANNEL] 채널 메시지 목록 조회 요청: channelId={}, cursor={}, pageSize={}",
        channelId, createdAt, pageable.getPageSize()
    );
    Slice<MessageDto> slice = messageRepository.findAllByChannel_IdWithAuthor(channelId,
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
    log.info("[MESSAGE_UPDATE] 메시지 수정 요청: messageId={}", messageId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new MessageNotFoundException(Map.of("messageId", messageId)));
    message.update(request.newContent());

    log.info("[MESSAGE_UPDATE] 메시지 수정 완료: messageId={}", messageId);
    return messageMapper.toDto(message);
  }

  @Transactional
  @Override
  public void delete(UUID messageId) {
    log.info("[MESSAGE_DELETE] 메시지 삭제 요청: messageId={}", messageId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new MessageNotFoundException(Map.of("messageId", messageId)));

    // 영속성 전이 + 고아 객체 제거
    messageRepository.delete(message);
    log.info("[MESSAGE_DELETE] 메시지 삭제 완료: messageId={}", messageId);
  }
}
