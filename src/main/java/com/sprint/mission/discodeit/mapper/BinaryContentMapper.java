package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BinaryContentMapper {

    public BinaryContentDto toDto(BinaryContent binaryContent) {
        if (binaryContent == null) return null;

        return new BinaryContentDto(
                binaryContent.getId(),
                binaryContent.getFileName(),
                binaryContent.getSize(),
                binaryContent.getContentType(),
                null
        );
    }

    public List<BinaryContentDto> toListDto(List<BinaryContent> binaryContents) {
        if (binaryContents == null || binaryContents.isEmpty()) return Collections.emptyList();

        return binaryContents.stream()
                .map(this::toDto)
                .toList();
    }

    public BinaryContentResponse toResponse(BinaryContent binaryContent) {
        if (binaryContent == null) return null;

        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getSize(),
                null
        );
    }

    public List<BinaryContentResponse> toListResponse(List<BinaryContent> binaryContents) {
        if (binaryContents == null || binaryContents.isEmpty()) return Collections.emptyList();

        return binaryContents.stream()
                .map(this::toResponse)
                .toList();
    }
}
