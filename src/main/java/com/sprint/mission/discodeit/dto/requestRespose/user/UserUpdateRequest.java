package com.sprint.mission.discodeit.dto.requestRespose.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 정보 수정 시 변경할 정보
public record UserUpdateRequest(
    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(max = 50, message = "사용자 이름은 최대 {max}자까지 입력할 수 있습니다.")
    String newUsername,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일 주소는 최대 {mas}자까지 입력할 수 있습니다.")
    String newEmail,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 60, message = "비밀번호는 {min}자 이상 {max}자 이하이어야 합니다.")
    String newPassword
    // BinaryContentRequest profileImage
    // 프로필 사진을 수정하지 않으면 null
    // 파일은 MultipartFile이라는 별도의 파라미터로 받기 때문에 해당 필드 삭제
) {

}