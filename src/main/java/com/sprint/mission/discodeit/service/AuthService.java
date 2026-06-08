package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.dto.requestRespose.auth.LoginRequest;

public interface AuthService {
    // 일치하는 유저가 있는 경우 UserResponse를 반환, 없으면 예외 발생
    UserDto login(LoginRequest request);
}
