package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserStatusMapper {
    public UserStatusResponse toResponse(UserStatus userStatus) {
        if (userStatus == null) return null;

        return new UserStatusResponse(
                userStatus.getId(),
                userStatus.getCreatedAt(),
                userStatus.getUpdatedAt(),
                userStatus.getUserId(),
                userStatus.getLastActiveAt(),
                userStatus.isOnline()
        );
    }

    public List<UserStatusResponse> toListResponse(List<UserStatus> userStatuses) {
        if (userStatuses == null || userStatuses.isEmpty()) return Collections.emptyList();

        return userStatuses.stream()
                .map(this::toResponse)
                .toList();
    }
}
