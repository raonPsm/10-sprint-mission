package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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

    // POST api/users -User 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> create(
            @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
                .flatMap(this::resolveProfileRequest);
        User createdUser = userService.create(userCreateRequest, profileRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }
    // TODO: Validation 추가 -> NotBlank, Email, Size...
    // TODO: Exception Handling
    // TODO: BinaryContent -> 프로필 이미지 기능 확인

    // 사용자 정보를 수정할 수 있다.
    // 멀티파트 타입 관련 수정
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @RequestPart("request") UserUpdateRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        BinaryContentCreateRequest profileRequest = toBinaryContentRequest(profile);

        UserResponse response = userService.update(id, request, profileRequest);
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

    // GET /api/users - 전체 User 목록 조회 O
    @GetMapping()
    public ResponseEntity<List<UserDto>> findAll() {
        List<UserDto> users = userService.findAllUsers();
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

    // ===

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
                throw new RuntimeException(e);
            }
        }
    }

    private BinaryContentCreateRequest toBinaryContentRequest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return new BinaryContentCreateRequest(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
        }
    }
}

