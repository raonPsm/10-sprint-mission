package com.sprint.mission.discodeit.dto.requestRespose.user;

import java.time.Instant;
import java.util.UUID;

// 사용자 정보를 조회할 때 반환
public record UserResponse (
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String username,
        String email,
        // 명세에는 password 반환이 있지만, 보안 이슈로 인해 password는 반환하지 않도록 수정
        // password는 반환하지 않음 (!보안 이슈!)
        UUID profileId,
        Boolean online // 접속 상태 (true: 온라인, false: 오프라인)
) {}
