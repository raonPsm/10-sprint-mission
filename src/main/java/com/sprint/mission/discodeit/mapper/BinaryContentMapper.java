package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BinaryContentMapper {
    @Mapping(target = "filename", source = "fileName")
    @Mapping(target = "bytes", expression = "java(null)")
    BinaryContentDto toDto(BinaryContent entity);

    List<BinaryContentDto> toDtoList(List<BinaryContent> entities);
}
