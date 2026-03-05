package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.requestRespose.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final UserStatusRepository userStatusRepository;

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        Boolean online = userStatusRepository.findByUserId(user.getId())
                .map(UserStatus::isOnline)
                .orElse(null);

        return new UserResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                // password는 반환 안함
                user.getProfileId(),
                online // TODO: N+1 문제?
        );
    }
}
