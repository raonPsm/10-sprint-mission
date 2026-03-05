package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.requestRespose.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

// TODO: (Later) 전반적으로 코드가 깔끔하지 않음 리팩토링 고려
@Component
@RequiredArgsConstructor
public class ChannelMapper {
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    // 단일 채널 매핑 (단건 조회용)
    public ChannelResponse toResponse(Channel channel) {
        if (channel == null) return null;

        // TODO: (Later) Repository에 특정 채널 ID로 조회하는 메서드(ex: findAllByChannelId) 구현
        Instant lastMessageAt = getLastMessageAt(channel);
        Set<UUID> participantIds = getParticipantIds(channel);

        return createChannelResponse(channel, lastMessageAt, participantIds);
    }

    // 다건 채널 매핑 (목록 조회용)
    // TODO: (Later) 가독성 향상 및 다른 방법 없는지 고민
    public List<ChannelResponse> toListResponse(List<Channel> channels) {
        if (channels == null || channels.isEmpty()) return Collections.emptyList();

        Set<UUID> channelIds = channels.stream()
                .map(Channel::getId)
                .collect(Collectors.toSet());

        // 1 필요한 데이터를 한 번에 조회하여 메모리에서 채널별로 그룹화
        // TODO: (Later) findAllByChannelIdIn(channelIds) 사용
        Map<UUID, Instant> lastMessageAtMap = messageRepository.findAll().stream()
                .filter(m -> channelIds.contains(m.getChannelId()))
                .collect(Collectors.toMap(
                        Message::getChannelId,
                        Message::getCreatedAt,
                        (existing, replacement) -> existing.compareTo(replacement) > 0 ? existing : replacement // 가장 나중에 생성된 시간 1개만 남음
                ));

        Map<UUID, Set<UUID>> participantIdsMap = readStatusRepository.findAll().stream()
                .filter(rs -> channelIds.contains(rs.getChannelId()))
                .collect(Collectors.groupingBy(
                        ReadStatus::getChannelId,
                        Collectors.mapping(ReadStatus::getUserId, Collectors.toSet())
                ));

        // 2 Response 생성
        return channels.stream()
                .map(channel -> {
                    Instant lastMessageAt = lastMessageAtMap.getOrDefault(channel.getId(), channel.getCreatedAt()); // 채널에 메시지 없으면 채널의 생성 시간 기본값으로 사용
                    Set<UUID> participantIds = channel.getType() == ChannelType.PRIVATE
                            ? participantIdsMap.getOrDefault(channel.getId(), Collections.emptySet())
                            : Collections.emptySet();

                    return createChannelResponse(channel, lastMessageAt, participantIds);
                })
                .collect(Collectors.toList());
    }

    private ChannelResponse createChannelResponse(Channel channel, Instant lastMessageAt, Set<UUID> participantIds) {
        return new ChannelResponse(
                channel.getId(),
                channel.getCreatedAt(),
                channel.getUpdatedAt(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                lastMessageAt,
                participantIds
        );
    }

    private Instant getLastMessageAt(Channel channel) {
        return messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(channel.getId()))
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(channel.getCreatedAt());
    }

    private Set<UUID> getParticipantIds(Channel channel) {
        if (channel.getType() != ChannelType.PRIVATE) {
            return Collections.emptySet();
        }
        return readStatusRepository.findAll().stream()
                .filter(rs -> rs.getChannelId().equals(channel.getId()))
                .map(ReadStatus::getUserId)
                .collect(Collectors.toSet());
    }
}