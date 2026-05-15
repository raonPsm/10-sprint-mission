package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

  private final UserService userService;

  @GetMapping("csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION) // 204가 더 적절해보이지만, 명세에 맞춤
        .build();
  }

  // @AuthenticationPrincipal
  //   - Principal을 Controller 메서드의 파라미터로 직접 주입받을 수 있게 해주는 어노테이션
  //   - 내부적으로 AuthenticationPrincipalArgumentResolver가 작동하여
  //     세션 내 SecurityContext의 Authentication 객체에서 principal을 꺼내온다.
  @GetMapping("me")
  public ResponseEntity<UserDto> me(@AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    log.info("사용자 본인 정보 조회 요청");
    UserDto userDto = userService.find(userDetails.getUserDto().id()); // 최신 정보 조회 후 반환
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDto);
  }
}
