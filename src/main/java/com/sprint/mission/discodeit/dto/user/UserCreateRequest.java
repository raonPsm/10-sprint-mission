package com.sprint.mission.discodeit.dto.user;

/*
record가 자동으로 해주는 것들
- 모든 필드는 private final - 생성 후에는 값을 바꿀 수 없음(불변성 보장)
- 생성자 - 모든 필드를 받는 생성자를 자동으로 생성
- Getter 메서드 (주의! 이름 앞에 get이 붙지 않음 - username())
- equals(), hashCode(), toString() 자동 구현

// TODO: [Later] DTO 개념 관련 정리 필요
(정리 필요)
- 유효성 검증 로직을 DTO 클래스로 빼내어 핸들러 메서드의 간결함을 유지할 수 있다.
- 비용이 많이 드는 작업인 HTTP 요청의 수를 줄이기 위함
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// TODO: [Later] 다른 필드에도 @Valid 관련 설정 고려 -> 프론트에서 해주는 것도 확인 필요
// 회원가입 시 필요한 정보
public record UserCreateRequest (
        String username,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        String password
        // BinaryContentRequest profileImage
            // BinaryContentRequest DTO를 필드로 받음 (Null 가능)
            // 파일은 MultipartFile이라는 별도의 파라미터로 받기 때문에 해당 필드 삭제
) {}