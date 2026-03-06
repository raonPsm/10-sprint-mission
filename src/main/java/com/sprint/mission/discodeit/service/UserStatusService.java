package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.Dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.requestRespose.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.userstatus.UserStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatusDto create(UserStatusCreateRequest request);
    UserStatusDto find(UUID id);
    List<UserStatusDto> findAll();
    // PK 기준 수정
    UserStatusDto update(UUID id, UserStatusUpdateRequest request);
    // FK 기준 수정
    UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request);
    void delete(UUID id);
}