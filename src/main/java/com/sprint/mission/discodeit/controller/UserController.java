package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
// user -> users 복수형 사용
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserStatusService userStatusService;

    @Autowired
    public UserController(UserService userService, UserStatusService userStatusService) {
        this.userService = userService;
        this.userStatusService = userStatusService;
    }

    // 사용자를 등록할 수 있다.
    // 멀티파트 타입 관련 수정
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> create(
            @RequestPart("request") UserCreateRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        UserResponse response = userService.create(request, profile); // TODO: UserService도 수정해야 한다.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // TODO: Validation 추가 -> NotBlank, Email, Size...
    // TODO: Exception Handling
    // TODO: BinaryContent -> 프로필 이미지 기능 확인

    // 사용자 정보를 수정할 수 있다.
    @PutMapping(value = "/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @RequestBody UserUpdateRequest request
    ) {
        UserResponse response = userService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    // TODO: 존재하지 않는 사용자 수정 시도 -> 404 반환
    // TODO: 현재 API는 id만 알면 누구나 다른 사람의 정보를 수정할 수 있는 구조 -> 보안 문제

    // 사용자를 삭제할 수 있다.
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 모든 사용자를 조회할 수 있다.
    @GetMapping()
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> users = userService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }
    // TODO: 특정 사용자 조회? userService.find()
    // TODO: username 또는 email로 유저 검색 기능

    // 사용자의 온라인 상태를 업데이트할 수 있다.
    // isOnline 필드(일부)만 수정하므로 Patch로 변경
    @PatchMapping(value = "/{userId}/status")
    public ResponseEntity<UserStatusResponse> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequest request
    ) {
        UserStatusResponse response = userStatusService.updateByUserId(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

