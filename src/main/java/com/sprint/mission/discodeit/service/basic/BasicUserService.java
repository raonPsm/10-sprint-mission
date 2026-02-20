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

import java.util.*;
import java.util.function.Function;
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

    @Override
    public User create(UserCreateRequest userCreateRequest,
                       Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        String username = userCreateRequest.username();
        String email = userCreateRequest.email();
        String password = userCreateRequest.password();

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

        User user = new User(username, email, password, nullableProfileId);
        User createdUser = userRepository.save(user);

        UserStatus userStatus = new UserStatus(createdUser.getId());
        userStatusRepository.save(userStatus);

        return user;

        // TODO: 트랜잭션 롤백 필요성 존재 -> 추후 단계에서 고민
        // TODO: username 유효성 검사 로직 추가
        // TODO: email 중복 불가능 검사 로직 추가
        // TODO: password 유효성 검사 로직 추가
    }

    @Override
    public User findByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 유저가 존재하지 않습니다. (userId: " + userId + " )"));

        // UserStatus 조회 (데이터 무결성을 위해 없으면 예외 처리 - 트랜잭션 없는 상황에서는 발생할 일 없음)
        UserStatus status = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("유저 상태(접속 정보)데이터가 누락되었습니다. (userId: " + userId + " )"));

        return user;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다."));
                    return user;
                }).collect(Collectors.toList());
    }

    @Override
    public User update(UUID userId,
                       UserUpdateRequest userUpdateRequest,
                       Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. (userId: " + userId + " )"));

        String newUsername = userUpdateRequest.newUsername();
        String newEmail = userUpdateRequest.newEmail();
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

        String newPassword = userUpdateRequest.newPassword();
        user.update(newUsername, newEmail, newPassword, nullableProfileId);

        return userRepository.save(user);
    }

    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. (userId: " + userId + " )"));

        // 연관된 UserStatus 삭제
        userStatusRepository.deleteByUserId(userId);

        // 연관된 BinaryContent (프로필 이미지) 삭제
        if (user.getProfileId() != null) {
            binaryContentRepository.deleteById(user.getProfileId()); // TODO: deleteByUserId 사용 고려 요망
        }

        userRepository.deleteById(userId);
    }
}
