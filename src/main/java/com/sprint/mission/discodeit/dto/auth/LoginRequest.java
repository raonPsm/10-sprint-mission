package com.sprint.mission.discodeit.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

// 로그인 요청 DTO
public record LoginRequest (
        String username,
        String password
) {}