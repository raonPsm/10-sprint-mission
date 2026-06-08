package com.sprint.mission.discodeit.dto.requestRespose.auth;

import jakarta.validation.constraints.NotBlank;

// 로그인 요청 DTO
public record LoginRequest(
    @NotBlank(message = "사용자 이름은 필수입니다.")
    String username,
    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {

}