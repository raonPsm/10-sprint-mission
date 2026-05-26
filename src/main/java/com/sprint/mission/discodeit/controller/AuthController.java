package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtProperties;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtProperties jwtProperties;
  private final UserService userService;

  @GetMapping("csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        .build();
  }

  @GetMapping("me")
  public ResponseEntity<UserDto> me(@AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    log.info("사용자 본인 정보 조회 요청");
    UserDto userDto = userService.find(userDetails.getUserDto().id());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDto);
  }

  @PutMapping("role")
  public ResponseEntity<UserDto> updateRole(@Valid @RequestBody UserRoleUpdateRequest request) {
    log.info("사용자 권한 수정 요청: userId={}, newRole={}", request.userId(), request.newRole());
    UserDto updatedUser = userService.updateRole(request.userId(), request.newRole());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedUser);
  }

  @PostMapping("refresh")
  public ResponseEntity<JwtDto> refresh(
      // 요청 쿠키에서 "REFRESH_TOKEN" 값을 자동으로 꺼내줌
      // required = false -> 쿠키가 없어도 예외 발생 안함, 대신 null로 들어옴
      @CookieValue(value = "REFRESH_TOKEN", required = false) String refreshToken,
      HttpServletResponse response
  ) {
    // 쿠키 자체가 없는 경우
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UserDto userDto;
    try {
      // 리프레시 토큰 파싱 + 유효성 검사 + 사용자 정보 추출
      userDto = jwtTokenProvider.extractUserDtoFromRefreshToken(refreshToken);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 새 토큰 발급
    String newAccessToken = jwtTokenProvider.generateAccessToken(userDto);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDto);

    // 새 리프레시 토큰을 쿠키에 덮어씀 (기존 쿠키 교체)
    Cookie cookie = new Cookie("REFRESH_TOKEN", newRefreshToken);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) (jwtProperties.refreshTokenExpiry() / 1000));
    response.addCookie(cookie);

    return ResponseEntity.ok(new JwtDto(newAccessToken));
  }
}
