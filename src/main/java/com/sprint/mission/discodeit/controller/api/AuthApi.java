package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

  @Operation(summary = "CSRF 토큰 발급")
  @ApiResponse(responseCode = "203", description = "CSRF 토큰 발급 성공 (Set-Cookie 헤더에 포함)")
  ResponseEntity<Void> getCsrfToken(
      @Parameter(hidden = true) CsrfToken csrfToken // 클라이언트가 직접 전달하는 값이 아니므로 hidden = true
  );

  @Operation(summary = "현재 로그인 사용자 정보 조회")
  @ApiResponse(responseCode = "200", description = "현재 사용자 정보 반환")
  ResponseEntity<UserDto> me(
      @Parameter(hidden = true) @AuthenticationPrincipal DiscodeitUserDetails userDetails);
} 