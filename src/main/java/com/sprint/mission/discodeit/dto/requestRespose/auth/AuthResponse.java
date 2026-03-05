package com.sprint.mission.discodeit.dto.requestRespose.auth;

import java.util.UUID;

// 로그인 결과 응답 DTO
public record AuthResponse (
        UUID userId,
        String username,
        String email
) {}
