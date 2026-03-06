package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.UserStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReadStatusMapper {
    @Mapping(target = "userId", source = "user.id")
    UserStatusDto toDto(ReadStatus entity);

    List<UserStatusDto> toDtoList(List<ReadStatus> entities);
}
