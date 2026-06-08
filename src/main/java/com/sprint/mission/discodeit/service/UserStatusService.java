package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatus create(UserStatusCreateRequest request);
    UserStatus find(UUID id);
    List<UserStatus> findAll();
    // PK 기준 수정
    UserStatus update(UUID id, UserStatusUpdateRequest request);
    // FK 기준 수정
    UserStatus updateByUserId(UUID userId, UserStatusUpdateRequest request);
    void delete(UUID id);
}