package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.UserApi;
import com.sprint.mission.discodeit.controller.exception.FileProcessException;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;
    private final UserStatusService userStatusService;
    private final UserMapper userMapper;

    /// GET /api/users - 전체 User 목록 조회
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> responseList = userService.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responseList);
    }
    // TODO: [Later] 특정 사용자 조회기능 추가 - userService.find() - username 또는 email로 유저 검색 기능

    /// POST api/users - User 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> create(
                    @Valid @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,
                    @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
                .flatMap(this::resolveProfileRequest);

        User createdUser = userService.create(userCreateRequest, profileRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userMapper.toResponse(createdUser));
    }
    // TODO: (Later) Validation 추가 -> NotBlank, Email, Size...
    // TODO: (Later) 커스텀 예외 클래스 생성

    /// DELETE /api/users/{userId} - User 삭제
    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
    // TODO: (Later) 삭제 요청한 주체 본인 or 관리자 확인

    /// PATCH /api/users/{userId} - User 정보 수정
    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID userId,
            @RequestPart("userUpdateRequest") UserUpdateRequest userUpdateRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
                .flatMap(this::resolveProfileRequest);

        User updateUser = userService.update(userId, userUpdateRequest, profileRequest);

        return ResponseEntity.ok(userMapper.toResponse(updateUser));
    }
    // TODO: (Later) 현재 API는 id만 알면 누구나 다른 사람의 정보를 수정할 수 있는 구조 -> 보안 문제

    /// PATCH /api/users/{userId}/userStatus - User 온라인 상태 업데이트
    @PatchMapping(value = "/{userId}/userStatus")
    public ResponseEntity<UserStatusResponse> updateUserStatusByUserId(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequest userStatusUpdateRequest
    ) {
        UserStatusResponse response = userStatusService.updateByUserId(userId, userStatusUpdateRequest);
        return ResponseEntity.ok(response);
    }

    // === Helper method ===
    // 사용자가 파일을 보냈는데, 서버가 어떤 이유로 그 파일을 정상적으로 읽어내지 못했을 때 발생하는 예외를 처리하기 위한 메서드
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

