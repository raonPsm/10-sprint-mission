package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.ChannelDto;
import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class}
)
public abstract class ChannelMapper {

    @Autowired protected MessageRepository messageRepository;
    @Autowired protected ReadStatusRepository readStatusRepository;
    @Autowired protected UserMapper userMapper;

    // target: 결과 DTO의 어떤 필드에 대한 설정인지 지정
    // ignore = true: 그 target 필드는 MapStruct 자동 매핑에서 제외
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "lastMessageAt", ignore = true)
    protected abstract ChannelDto toBaseDto(Channel entity);

    public ChannelDto toDto(Channel entity) {
        ChannelDto base = toBaseDto(entity);

        Instant lastMessageAt = messageRepository
                .findTopByChannel_IdOrderByCreatedAtDesc(entity.getId())
                .map(Message::getCreatedAt)
                .orElse(entity.getCreatedAt());

        List<UserDto> participants = entity.getType() == ChannelType.PRIVATE
                ? readStatusRepository.findAllByChannel_Id(entity.getId()).stream()
                .map(ReadStatus::getUser)
                .map(userMapper::toDto)
                .toList()
                : List.of();

        return new ChannelDto(
                base.id(),
                base.type(),
                base.name(),
                base.description(),
                participants,
                lastMessageAt
        );
    }

    public List<ChannelDto> toDtoList(List<Channel> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}
