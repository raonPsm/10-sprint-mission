package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BinaryContentMapper {

    // 단건 변환
    public BinaryContentResponse toResponse(BinaryContent binaryContent) {
        if (binaryContent == null) return null;

        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getSize(),
                binaryContent.getBytes()
        );
    }

    // 다건 리스트 변환
    public List<BinaryContentResponse> toListResponse(List<BinaryContent> binaryContents) {
        if (binaryContents == null || binaryContents.isEmpty()) return Collections.emptyList();

        return binaryContents.stream()
                .map(this::toResponse)
                .toList();
    }
}
