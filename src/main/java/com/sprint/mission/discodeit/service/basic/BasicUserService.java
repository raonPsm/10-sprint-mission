package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
  // @Component의 자식이므로 빈으로 자동으로 등록해 줌
  // 해당 클래스가 service 계층임을 명시
  // 스프링은 Service 계층을 트랜잭션의 시작과 끝으로 봄
@RequiredArgsConstructor
  // final이 붙은 필드를 초기화하는 생성자를 자동으로 생성한다. -> Lombok 적용
  // Lombok이 알아서 만들어 주기 때문에 Repository가 하나 더 늘어나도 생성자 크드를 수정할 필요가 없다.
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentService binaryContentService;

    @Override
    public User create(UserCreateRequest userCreateRequest,
                       Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        String username = userCreateRequest.username();
        String email = userCreateRequest.email();

        // email, username 중복 불가능
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
        String password = userCreateRequest.password();

        User user = new User(username, email, password, nullableProfileId);
        User createdUser = userRepository.save(user);

        UserStatus userStatus = new UserStatus(createdUser.getId());
        userStatusRepository.save(userStatus);

        return createdUser;

        // TODO: 트랜잭션 롤백 필요성 존재 -> 추후 단계에서 고민
        // TODO: username 유효성 검사 로직 추가
        // TODO: email 중복 불가능 검사 로직 추가
        // TODO: password 유효성 검사 로직 추가
    }

    @Override
    public UserResponse find(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 유저가 존재하지 않습니다. (userId: " + userId + " )"));

        // UserStatus 조회 (데이터 무결성을 위해 없으면 예외 처리 - 트랜잭션 없는 상황에서는 발생할 일 없음)
        UserStatus status = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("유저 상태(접속 정보)데이터가 누락되었습니다. (userId: " + userId + " )"));

        return toResponse(user, status);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(user -> {
                    // 각 유저의 상태 정보 조회 // TODO: N+1 문제로 최적화 필요. 현재는 단순 조회 (Later)
                    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다."));
                    return toResponse(user, userStatus);
                }).collect(Collectors.toList());
    }

    @Override
    public User update(UUID userId,
                       UserUpdateRequest userUpdateRequest,
                       Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. id: " + userId));

        String newUsername = userUpdateRequest.username();
        String newEmail = userUpdateRequest.email();
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용중인 이메일(email)입니다: " + newEmail);
        }
        if (userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름(username)입니다.: " + newUsername);
        }

        UUID nullableProfileId = optionalProfileCreateRequest
                .map(profileRequest -> {
                    Optional.ofNullable(user.getProfileId())
                            .ifPresent(binaryContentRepository::deleteById);

                    String fileName = profileRequest.fileName();
                    String contentType = profileRequest.contentType();
                    byte[] bytes = profileRequest.bytes();
                    BinaryContent binaryContent = new BinaryContent(fileName, contentType, bytes);
                    return binaryContentRepository.save(binaryContent).getId();
                })
                .orElse(null);

        String newPassword = userUpdateRequest.password();
        user.update(newUsername, newEmail, newPassword, nullableProfileId);

        return userRepository.save(user);
    }

    @Override
    public void delete(UUID userId) {
        // TODO: findById() 활용해서 중복되는 부분 제거할 수 있는지 확인 필요
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. id: " + userId));

        // 연관된 UserStatus 삭제
        userStatusRepository.deleteByUserId(userId);

        // 연관된 BinaryContent (프로필 이미지) 삭제
        if (user.getProfileId() != null) {
            binaryContentRepository.deleteById(user.getProfileId()); // TODO: deleteByUserId 사용 고려 요망
        }

        // User 삭제
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다."));

                    return new UserDto(
                            user.getId(),
                            user.getCreatedAt(),
                            user.getUpdatedAt(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getProfileId(),
                            userStatus.isOnline()
                    );
                }).collect(Collectors.toList());
    }

    // ===

    // DTO -> BinaryContent 변환 로직
    private BinaryContent createBinaryContent(BinaryContentCreateRequest request) {
        return new BinaryContent(
                request.fileName(),
                request.contentType(),
                request.bytes()
        );
    }

    // Entity -> DTO 변환 로직
    private UserResponse toResponse(User user, UserStatus userStatus) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                userStatus.isOnline()
        );
    }
}
