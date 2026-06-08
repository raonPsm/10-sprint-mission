package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ReadStatusMapper {
    public ReadStatusResponse toResponse(ReadStatus readStatus) {
        if (readStatus == null) return null;

        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
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
