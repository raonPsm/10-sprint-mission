package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.dto.requestRespose.auth.LoginRequest;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/auth")
public class AuthController implements AuthApi {

  private final AuthService authService;

  // POST /api/auth/login - 로그인
  @PostMapping("/login")
  public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest request) {
    UserDto response = authService.login(request);
    return ResponseEntity.ok(response); // 로그인 성공 시 사용자 정보를 반환
  }

  // TODO: 로그인 실패 시 예외처리

  // TODO: 로그아웃 구현
  // TODO: 인증상태 유지 -> Session, JWT
  // TODO: 중복 로그인 방지
}
