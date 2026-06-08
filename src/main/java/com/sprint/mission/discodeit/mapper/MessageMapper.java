package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class, BinaryContentMapper.class}
)
public interface MessageMapper {
    // @Mapping -> 기본적으로 이름/타입이 같으면 자동 매핑, 이름이 다르거나 경로가 필요하면 @Mapping 사용
    // target : MessageDto의 필드명
    // source : Message에서 가져올 값의 경로
    @Mapping(target = "channelId", source = "channel.id")
    MessageDto toDto(Message entity);

    List<MessageDto> toDtoList(List<Message> entities);
}
