package com.sprint.mission.discodeit.dto.user;

// 정보 수정 시 변경할 정보
public record UserUpdateRequest (
        String username,
        String email,
        String password
        // BinaryContentRequest profileImage
            // 프로필 사진을 수정하지 않으면 null
            // 파일은 MultipartFile이라는 별도의 파라미터로 받기 때문에 해당 필드 삭제
) {}