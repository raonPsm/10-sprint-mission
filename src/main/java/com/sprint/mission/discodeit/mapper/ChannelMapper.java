package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.ChannelDto;
import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class}
)
public abstract class ChannelMapper {
    @Autowired protected MessageRepository messageRepository;
    @Autowired protected ReadStatusRepository readStatusRepository;
    @Autowired protected UserMapper userMapper;

    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "lastMessageAt", ignore = true)
    public abstract ChannelDto toDto(Channel entity);

    public abstract List<ChannelDto> toDtoList(List<Channel> entities);

    @AfterMapping
    protected void enrich(Channel entity, @MappingTarget ChannelDto dto) {
        Instant lastMessageAt = messageRepository.findAll().stream()
                .filter(msg -> msg.getChannel().getId().equals(entity.getId()))
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(entity.getCreatedAt());

        List<UserDto> participants = entity.getType() == ChannelType.PRIVATE
                ? readStatusRepository.findAll().stream()
                    .filter(rs -> rs.getChannel().getId().equals(entity.getId()))
                    .map(ReadStatus::getUser)
                    .map(userMapper::toDto)
                    .toList()
                : Collections.emptyList();

        dto = new ChannelDto (
                dto.id(),
                dto.type(),
                dto.name(),
                dto.description(),
                dto.participants(),
                dto.lastMessageAt()
        );
    }
}
