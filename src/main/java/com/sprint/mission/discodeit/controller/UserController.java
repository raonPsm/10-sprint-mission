package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.UserApi;
import com.sprint.mission.discodeit.controller.exception.FileProcessException;
import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.dto.Dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;
  private final UserStatusService userStatusService;

  /// GET /api/users - 전체 User 목록 조회
  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    List<UserDto> responseList = userService.findAll();
    return ResponseEntity.ok(responseList);
  }
  // TODO: [Later] 특정 사용자 조회기능 추가 - userService.find() - username 또는 email로 유저 검색 기능

  /// POST api/users - User 등록
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> create(
      @Valid @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
        .flatMap(this::resolveProfileRequest);

    UserDto createdUser = userService.create(userCreateRequest, profileRequest);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdUser);
  }

  /// DELETE /api/users/{userId} - User 삭제
  @DeleteMapping(value = "/{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }
  // TODO: (Later) 삭제 요청한 주체 본인 or 관리자 확인

  /// PATCH /api/users/{userId} - User 정보 수정
  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> update(
      @PathVariable UUID userId,
      @Valid @RequestPart UserUpdateRequest userUpdateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
        .flatMap(this::resolveProfileRequest);

    UserDto updateUser = userService.update(userId, userUpdateRequest, profileRequest);

    return ResponseEntity.ok(updateUser);
  }
  // TODO: (Later) 현재 API는 id만 알면 누구나 다른 사람의 정보를 수정할 수 있는 구조 -> 보안 문제

  /// PATCH /api/users/{userId}/userStatus - User 온라인 상태 업데이트
  @PatchMapping(value = "/{userId}/userStatus")
  public ResponseEntity<UserStatusDto> updateUserStatusByUserId(
      @PathVariable UUID userId,
      @Valid @RequestBody UserStatusUpdateRequest userStatusUpdateRequest
  ) {
    UserStatusDto response = userStatusService.updateByUserId(userId, userStatusUpdateRequest);
    return ResponseEntity.ok(response);
  }

  // === Helper method ===
  // 사용자가 파일을 보냈는데, 서버가 어떤 이유로 그 파일을 정상적으로 읽어내지 못했을 때 발생하는 예외를 처리하기 위한 메서드
  // 중복되는 메서드 제거 위함
  private Optional<BinaryContentCreateRequest> resolveProfileRequest(MultipartFile profileFile) {
    if (profileFile.isEmpty()) {
      return Optional.empty();
    } else {
      try {
        BinaryContentCreateRequest binaryContentCreateRequest = new BinaryContentCreateRequest(
            profileFile.getOriginalFilename(),
            profileFile.getContentType(),
            profileFile.getBytes()
        );
        return Optional.of(binaryContentCreateRequest);
      } catch (IOException e) {
        throw new FileProcessException("프로필 파일 처리 중 오류가 발생했습니다.", e);
      }
    }
  }
}

