package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReadStatusMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "channelId", source = "channel.id")
    ReadStatusDto toDto(ReadStatus entity);

    List<ReadStatusDto> toDtoList(List<ReadStatus> entities);
}
