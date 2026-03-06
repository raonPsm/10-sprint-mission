package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.ChannelDto;
import com.sprint.mission.discodeit.dto.Dto.UserDto;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChannelMapper {
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserMapper userMapper;

    public ChannelDto toDto(Channel channel) {
        if (channel == null) return null;

        return new ChannelDto(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                getParticipantDtos(channel),
                getLastMessageAt(channel)
        );
    }

    public List<ChannelDto> toListDto(List<Channel> channels) {
        if (channels == null || channels.isEmpty()) return Collections.emptyList();

        return channels.stream()
                .map(this::toDto)
                .toList();
    }

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

    public List<ChannelResponse> toListResponse(List<Channel> channels) {
        if (channels == null || channels.isEmpty()) return Collections.emptyList();

        return channels.stream()
                .map(this::toResponse)
                .toList();
    }

    private Instant getLastMessageAt(Channel channel) {
        return messageRepository.findAll().stream()
                .filter(m -> m.getChannel().getId().equals(channel.getId()))
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(channel.getCreatedAt());
    }

    private Set<UUID> getParticipantIds(Channel channel) {
        if (channel.getType() != ChannelType.PRIVATE) {
            return Collections.emptySet();
        }

        return readStatusRepository.findAll().stream()
                .filter(rs -> rs.getChannel().getId().equals(channel.getId()))
                .map(rs -> rs.getUser().getId())
                .collect(Collectors.toSet());
    }

    private List<UserDto> getParticipantDtos(Channel channel) {
        if (channel.getType() != ChannelType.PRIVATE) {
            return Collections.emptyList();
        }

        return readStatusRepository.findAll().stream()
                .filter(rs -> rs.getChannel().getId().equals(channel.getId()))
                .map(ReadStatus::getUser)
                .map(userMapper::toDto)
                .toList();
    }
}
