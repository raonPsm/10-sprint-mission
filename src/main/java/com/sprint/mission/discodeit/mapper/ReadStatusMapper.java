package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ReadStatusMapper {
    public ReadStatusDto toDto(ReadStatus readStatus) {
        if (readStatus == null) return null;

        return new ReadStatusDto(
                readStatus.getId(),
                readStatus.getUser().getId(),
                readStatus.getChannel().getId(),
                readStatus.getLastReadAt()
        );
    }

    public List<ReadStatusDto> toListDto(List<ReadStatus> readStatuses) {
        if (readStatuses == null || readStatuses.isEmpty()) return Collections.emptyList();

        return readStatuses.stream()
                .map(this::toDto)
                .toList();
    }

    public ReadStatusResponse toResponse(ReadStatus readStatus) {
        if (readStatus == null) return null;

        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt(),
                readStatus.getUser().getId(),
                readStatus.getChannel().getId(),
                readStatus.getLastReadAt()
        );
    }

    public List<ReadStatusResponse> toListResponse(List<ReadStatus> readStatuses) {
        if (readStatuses == null || readStatuses.isEmpty()) return Collections.emptyList();

        return readStatuses.stream()
                .map(this::toResponse)
                .toList();
    }
}
