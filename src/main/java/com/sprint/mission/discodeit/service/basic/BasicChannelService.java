package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Dto.ChannelDto;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNameAlreadyExistsException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelAlreadyExistsException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelParticipantsEmptyException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final ChannelMapper channelMapper;
  // MessageRepository 의존성 제거 <- Cascade 적용

  @Transactional
  @Override
  public ChannelDto create(PublicChannelCreateRequest request) {

    // 채널 이름 중복 검사
    if (channelRepository.existsByName(request.name())) {
      log.warn("[CHANNEL_CREATE_PUBLIC] 이미 존재하는 공개 채널 이름: name={}", request.name());
      throw new ChannelNameAlreadyExistsException(Map.of("name", request.name()));
    }

    // PUBLIC 채널 생성
    Channel channel = Channel.createPublicChannel(
        request.name(),
        request.description()
    );

    ChannelDto result = channelMapper.toDto(channelRepository.save(channel));
    log.info("[CHANNEL_CREATE_PUBLIC] 공개 채널 생성 완료: channelId={}, name={}", result.id(),
        result.name());
    return result;
  }

  @Transactional
  @Override
  public ChannelDto create(PrivateChannelCreateRequest request) {
    Set<UUID> requestedUserIds = request.participantIds();

    // 유효성 검증 - 참여자가 없는 경우 검증
    if (requestedUserIds == null || requestedUserIds.isEmpty()) {
      log.warn("[CHANNEL_CREATE_PRIVATE] 비공개 채널 생성 실패: 참여자 없음");
      throw new PrivateChannelParticipantsEmptyException(Map.of());
    }

    // 유효성 검증 - 동일 참여자 구성의 비공개 채널 존재 여부
    channelRepository.findPrivateChannelByParticipantsIds(requestedUserIds)
        .ifPresent(existingChannel -> {
          log.warn("[CHANNEL_CREATE_PRIVATE] 동일 참여자 구성의 비공개 채널 이미 존재: existingChannelId={}",
              existingChannel.getId()
          );
          throw new PrivateChannelAlreadyExistsException(
              Map.of("existingChannelId", existingChannel.getId())
          );
        });

    // 유효성 검증 - 유저 존재 여부 검증
    for (UUID userId : requestedUserIds) {
      if (!userRepository.existsById(userId)) {
        throw new UserNotFoundException(Map.of("userId", userId));
      }
    }

    // PRIVATE 채널 생성
    Channel channel = Channel.createPrivateChannel();
    Channel savedChannel = channelRepository.save(channel);

    // 참여자별 ReadStatus 생성
    Instant now = Instant.now(); // 루프 내에서 호출 X
    List<User> users = userRepository.findAllById(requestedUserIds);
    List<ReadStatus> readStatuses = users.stream()
        .map(user -> new ReadStatus(user, savedChannel, now))
        .toList();
    readStatusRepository.saveAll(readStatuses);

    log.info("[CHANNEL_CREATE_PRIVATE] 비공개 채널 생성 완료: channelId={}", savedChannel.getId());
    return channelMapper.toDto(savedChannel);
  }

  @Transactional(readOnly = true)
  @Override
  public ChannelDto find(UUID channelId) {
    return channelMapper.toDto(channelRepository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(Map.of("channelId", channelId)))
    );
  }

  // 특정 유저가 볼 수 있는 Channel 목록을 조회
  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    // 모든 채널 조회 -> PUBLIC 채널은 모두 포함 / PRIVATE 채널은 해당 유저가 ReadStatus를 가지고 있는 경우만 포함

    Set<UUID> accessiblePrivateChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(rs -> rs.getChannel().getId())
        .collect(Collectors.toSet());

    return channelMapper.toDtoList(
        channelRepository.findAllByAccessible(accessiblePrivateChannelIds));
  }

  @Transactional
  @Override
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(Map.of("channelId", channelId)));

    // PRIVATE 채널은 수정 불가
    if (channel.getType() == ChannelType.PRIVATE) {
      throw new PrivateChannelUpdateNotAllowedException(Map.of("channelId", channelId));
    }

    // 채널 이름 중복 검사
    if (request.newName() != null && !request.newName().equals(channel.getName())) {
      if (channelRepository.existsByName(request.newName())) {
        throw new ChannelNameAlreadyExistsException(Map.of("name", request.newName()));
      }
    }

    channel.update(request.newName(), request.newDescription());

    log.info("[CHANNEL_UPDATE] 채널 수정 완료: channelId={}, name={}", channelId, channel.getName());
    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  public void delete(UUID channelId) {
    // TODO: Channel channel = findByChannelId(channelId); vs Helper Method -> 어떤 방식이 더 나은지?
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(Map.of("channelId", channelId)));

    messageRepository.deleteAllByChannel_Id(channelId);
    readStatusRepository.deleteAllByChannel_Id(channelId);

    channelRepository.delete(channel);
    log.info("[CHANNEL_DELETE] 채널 삭제 완료: channelId={}", channelId);
  }
}
