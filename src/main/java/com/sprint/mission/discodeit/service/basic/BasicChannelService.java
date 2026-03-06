package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.requestRespose.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    // MessageRepository 의존성 제거 <- Cascade 적용

    @Transactional
    @Override
    public Channel create(PublicChannelCreateRequest request) {
        // 채널 이름 중복 검사
        if(channelRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 공개 채널 이름(name)입니다. " + request.name());
        }

        // PUBLIC 채널 생성
        Channel channel = Channel.createPublicChannel(
                request.name(),
                request.description()
        );
        return channelRepository.save(channel);
    }

    @Transactional
    @Override
    public Channel create(PrivateChannelCreateRequest request) {
        Set<UUID> requestedUserIds = request.participantIds();

        // 유효성 검증 - 참여자가 없는 경우 검증
        if (requestedUserIds == null || requestedUserIds.isEmpty()) {
            throw new IllegalArgumentException("비공개 채널 참여자가 지정되지 않았습니다.");
        }

        // 유효성 검증 - 동일 참여자 구성의 비공개 채널 존재 여부
        channelRepository.findPrivateChannelByParticipantsIds(requestedUserIds).ifPresent(existingChannel -> {
            throw new IllegalArgumentException("동일한 유저로 구성된 비공개 채널이 존재합니다.");
        });

        // 유효성 검증 - 유저 존재 여부 검증
        for (UUID userId : requestedUserIds) {
            if (!userRepository.existsById(userId)) {
                throw new NoSuchElementException("유저가 존재하지 않습니다. id: " + userId);
            }
        }

        // PRIVATE 채널 생성
        Channel channel = Channel.createPrivateChannel();
        Channel savedChannel = channelRepository.save(channel);

        // 참여자별 ReadStatus 생성
        for (UUID userId : requestedUserIds) {
            User userProxy = userRepository.getReferenceById(userId);
            ReadStatus readStatus = new ReadStatus(userProxy, savedChannel, Instant.now());
            readStatusRepository.save(readStatus);
        }

        return savedChannel;
    }

    @Transactional(readOnly = true)
    @Override
    public Channel findByChannelId(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("해당 채널이 존재하지 않습니다. channelId: " + channelId));
    }

    // 특정 유저가 볼 수 있는 Channel 목록을 조회
    @Transactional(readOnly = true)
    @Override
    public List<Channel> findAllByUserId(UUID userId) {
        // 모든 채널 조회 -> PUBLIC 채널은 모두 포함 / PRIVATE 채널은 해당 유저가 ReadStatus를 가지고 있는 경우만 포함

        Set<UUID> accessiblePrivateChannelIds = readStatusRepository.findAllByUserId(userId).stream()
                .map(rs -> rs.getChannel().getId())
                .collect(Collectors.toSet());

        return channelRepository.findAllByAccessible(accessiblePrivateChannelIds);
    }

    @Transactional
    @Override
    public Channel update(UUID channelId, PublicChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("해당 채널이 존재하지 않습니다. id: " + channelId));

        // PRIVATE 채널은 수정 불가
        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("비공개(PRIVATE) 채널은 수정할 수 없습니다. name, description 수정 불가능.");
        }

        // 채널 이름 중복 검사
        if (request.newName() != null && !request.newName().equals(channel.getName())) {
            if (channelRepository.existsByName(request.newName())) {
                throw new IllegalArgumentException("이미 존재하는 공개 채널 이름(name)입니다. " + request.newName());
            }
        }

        channel.update(request.newName(), request.newDescription());

        return channel;
    }

    @Transactional
    @Override
    public void delete(UUID channelId) {
        // TODO: Channel channel = findByChannelId(channelId); vs Helper Method -> 어떤 방식이 더 나은지?
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("해당 채널이 존재하지 않습니다. id: " + channelId));

        messageRepository.deleteAllByChannel_Id(channelId);
        readStatusRepository.deleteAllByChannel_Id(channelId);

        channelRepository.delete(channel);
    }
}
