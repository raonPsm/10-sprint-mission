package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;

    @Transactional
    @Override
    public User create(UserCreateRequest userCreateRequest,
                       Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        String username = userCreateRequest.username();
        String email = userCreateRequest.email();
        String password = userCreateRequest.password();

        // email, username 중복 검증
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일(email)입니다: " + email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름(username)입니다.: " + username);
        }

        // 프로필 이미지 처리
        UUID nullableProfileId = optionalProfileCreateRequest
                .map(profileRequest -> {
                    String fileName = profileRequest.fileName();
                    String contentType = profileRequest.contentType();
                    byte[] bytes = profileRequest.bytes();

                    BinaryContent binaryContent = new BinaryContent(fileName, contentType, bytes);
                    return binaryContentRepository.save(binaryContent).getId();
                })
                .orElse(null);

        // 유저 생성 및 젖아
        User user = new User(username, email, password, nullableProfileId);
        User createdUser = userRepository.save(user);

        // 유저 상태 초기화
        UserStatus userStatus = new UserStatus(createdUser.getId(), Instant.now());
        userStatusRepository.save(userStatus);

        return createdUser;

        // TODO: 트랜잭션 롤백 필요성 존재 -> 추후 단계에서 고민
        // TODO: username 유효성 검사 로직 추가
        // TODO: email 중복 불가능 검사 로직 추가
        // TODO: password 유효성 검사 로직 추가
    }

    @Override
    public User findByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 유저가 존재하지 않습니다. (userId: " + userId + " )"));

        // UserStatus 조회 (데이터 무결성을 위해 없으면 예외 처리)
        validateUserStatusExists(userId);

        return user;
    }

    @Override
    public List<User> findAll() {
        List<User> users = userRepository.findAll();

        Map<UUID, UserStatus> statusMap = userStatusRepository.findAll().stream()
                .collect(Collectors.toMap(
                        UserStatus::getUserId,
                        Function.identity()
                ));
        return users.stream()
                .map(user -> { // TODO: [Later] .peek()로 변경 고려
                    if (!statusMap.containsKey(user.getId())) {
                        throw new NoSuchElementException("UserStatus를 찾을 수 없습니다.");
                        // TODO: [Later] 목록 조회에서 예외 던지는 것 보다는 로그를 남기거나 내부적으로 처리 하는 방향으로
                    }
                    return user;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public User update(UUID userId,
                       UserUpdateRequest userUpdateRequest,
                       Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        // 유저 존재 확인
        User user = findByUserId(userId);

        String newUsername = userUpdateRequest.newUsername();
        String newEmail = userUpdateRequest.newEmail();

        // FIX: 본인의 현재 정보와 다를 때만 중복 검사 실시
        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용중인 이메일(email)입니다.: " + newEmail);
        }
        if (!user.getUsername().equals(newUsername) && userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름(username)입니다.: " + newUsername);
        }

        // 회원가입 시 사진을 등록했다면 기본 프로필로 돌아갈 수 있는 기능 없음
        // -> 업데이트 시 파일이 존재하면 기존 사진 삭제 후 새로 저장, 파일이 없으면 기존 사진 유지
        UUID nullableProfileId = resolveNullableProfileId(user, optionalProfileCreateRequest);

        // user 업데이트 및 저장
        user.update(newUsername, newEmail, userUpdateRequest.newPassword(), nullableProfileId);
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void delete(UUID userId) {
        // 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. (userId: " + userId + " )"));

        // 연관된 UserStatus 삭제
        userStatusRepository.deleteByUserId(userId);

        // 연관된 BinaryContent (프로필 이미지) 삭제
        Optional.ofNullable(user.getProfileId())
                .ifPresent(binaryContentRepository::deleteById); // TODO: [Later] deleteByUserId 사용 고려 및 판단 필요

        userRepository.deleteById(userId);
    }

    // === Helper Method ===

    private void validateUserStatusExists(UUID userId) {
        if (!userStatusRepository.existsByUserId(userId)) {
            throw new IllegalStateException("유저 상태 데이터가 누락되었습니다. (userId: " + userId + ")");
        }
    }

    // FIX: 별도 메서드 분리
    // TODO: 별도 메서드 분리 기준 고민
    // (프로필 이미지 업데이트 로직이 UserService에 존재하는 것이 적절한지, BinaryContentService로 분리하는 것이 나은지)
    private UUID resolveNullableProfileId(User user,
                                          Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {

        return optionalProfileCreateRequest
                .map(profileRequest -> {
                    Optional.ofNullable(user.getProfileId())
                            .ifPresent(binaryContentRepository::deleteById);

                    BinaryContent binaryContent = new BinaryContent(
                            profileRequest.fileName(),
                            profileRequest.contentType(),
                            profileRequest.bytes()
                    );
                    return binaryContentRepository.save(binaryContent).getId();
                })
                .orElse(user.getProfileId());
    }
}
