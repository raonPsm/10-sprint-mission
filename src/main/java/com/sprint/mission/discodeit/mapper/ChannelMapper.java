package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
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

@Component
@RequiredArgsConstructor
public class ChannelMapper {
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    public ChannelResponse toResponse(Channel channel) {
        if (channel == null) return null;

        Instant lastMessageAt = getLastMessageAt(channel);
        Set<UUID> participantIds = getParticipantIds(channel);

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

    // === Helper method ===

    // 1 마지막 메시지 시간 조회 로직
    private Instant getLastMessageAt(Channel channel) {
        return messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(channel.getId()))
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(channel.getCreatedAt()); // 메시지가 없으면 채널 생성 시간 반환
    }

    // 2 참여자 ID 목록 조회 로직
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
