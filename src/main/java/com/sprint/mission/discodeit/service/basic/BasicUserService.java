package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserStatusMissingException;
import com.sprint.mission.discodeit.exception.user.UsernameAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  // binaryContentRepo, userStatusRepo -> 의존성 필요 없음 / 변경 감지 및 영속성 전이 활용
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  @Transactional
  @Override
  public UserDto create(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest
  ) {
    String username = userCreateRequest.username();
    String email = userCreateRequest.email();
    String password = userCreateRequest.password();

    // email, username 중복 검증
    if (userRepository.existsByEmail(email)) {
      log.warn("[USER_CREATE] 이미 사용중인 이메일: email={}", email);
      throw new EmailAlreadyExistsException(Map.of("email", email));
    }
    if (userRepository.existsByUsername(username)) {
      log.warn("[USER_CREATE] 이미 존재하는 사용자 이름: username={}", username);
      throw new UsernameAlreadyExistsException(Map.of("username", username));
    }

    // 프로필 이미지 엔티티 생성
    BinaryContent profileImage = optionalProfileCreateRequest
        .map(req -> {
          BinaryContent profile = new BinaryContent(
              req.fileName(),
              (long) req.bytes().length,
              req.contentType()
          );

          // DB에 메타데이터 저장
          binaryContentRepository.save(profile);

          // 실제 파일 데이터를 스토리지에 저장
          binaryContentStorage.put(profile.getId(), req.bytes());

          return profile;
        })
        .orElse(null);

    // User 엔티티 생성
    User user = new User(username, email, password, profileImage);

    // UserStatus 엔티티 생성 + 양방향 연관관계(1:1) 설정
    UserStatus userStatus = new UserStatus(user, Instant.now());
    user.assignUserStatus(userStatus); // TODO: 필요한 것인지 검토 요망

    User savedUser = userRepository.save(user);

    log.info("[USER_CREATE] 사용자 생성 완료: userId={}, username={}",
        savedUser.getId(), savedUser.getUsername()
    );
    return userMapper.toDto(savedUser);
    // TODO: username 유효성 검사 로직 추가
    // TODO: email 중복 불가능 검사 로직 추가
    // TODO: password 유효성 검사 로직 추가
  }

  @Transactional(readOnly = true)
  @Override
  public UserDto find(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(Map.of("userId", userId)));

    // UserStatus 무결성 검증
    if (user.getUserStatus() == null) {
      log.warn("[USER_FIND] 유저 상태 데이터 누락: userId={}", userId);
      throw new UserStatusMissingException(Map.of("userId", userId));
    }

    return userMapper.toDto(user);
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserDto> findAll() {
    // [1] N+1 문제 -> profile, UserStatus 한 번에 조회
    return userMapper.toDtoList(userRepository.findAllWithProfileAndUserStatus());
  }

  @Transactional
  @Override
  public UserDto update(UUID userId,
      UserUpdateRequest userUpdateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest
  ) {
    // user 존재하는지 조회
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new UserNotFoundException(Map.of("userId", userId)));

    String newUsername = userUpdateRequest.newUsername();
    String newEmail = userUpdateRequest.newEmail();

    // 본인의 현재 정보와 다를 때만 중복 검사 실시
    // TODO: (LATER) 원래 정보하고 같을 경우 다른 Exception 던지기
    if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
      log.warn("[USER_UPDATE] 이미 사용중인 이메일: email={}", newEmail);
      throw new EmailAlreadyExistsException(Map.of("email", newEmail));
    }
    if (!user.getUsername().equals(newUsername) && userRepository.existsByUsername(newUsername)) {
      log.warn("[USER_UPDATE] 이미 존재하는 사용자 이름: username={}", newUsername);
      throw new UsernameAlreadyExistsException(Map.of("username", newUsername));
    }

    // 회원가입 시 사진을 등록했다면 기본 프로필로 돌아갈 수 있는 기능 없음
    // -> 업데이트 시 파일이 존재하면 기존 사진 삭제 후 새로 저장, 파일이 없으면 기존 사진 유지
    BinaryContent newProfileImage = optionalProfileCreateRequest
        .map(req -> {
          BinaryContent newProfile = new BinaryContent(
              req.fileName(),
              (long) req.bytes().length,
              req.contentType()
          );
          binaryContentRepository.save(newProfile);
          binaryContentStorage.put(newProfile.getId(), req.bytes());
          return newProfile;
        })
        .orElse(user.getProfile());

    // orphanRemoval = true 설정으로 새로운 프로필 참조가 할당되면 기존 프로필은 자동으로 DELETE 수행됨
    user.update(newUsername, newEmail, userUpdateRequest.newPassword(), newProfileImage);

    log.info("[USER_UPDATE] 사용자 수정 완료: userId={}, username={}", userId, newUsername);
    // (Dirty Checking) 메서드 종료 시 트랜잭션이 커밋되면서 변경된 엔티티에 대한 UPDATE 쿼리 자동 발생. save() 호출 불필요
    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new UserNotFoundException(Map.of("userId", userId)));

    log.info("[USER_DELETE] 사용자 삭제 완료: userId={}", userId);

    // cascade = CascadeType.ALL, orphanRemoval = true 적용
    // User만 삭제해도 연관된 UserStatus, BinaryContent에 대한 DELETE 쿼리가 자동으로 발생
    userRepository.delete(user);
  }
}
