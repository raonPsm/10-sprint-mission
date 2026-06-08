package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UsernameAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

// @Mock, @Spy, @InjectMocks와 같은 Mockito 관련 어노테이션이 붙은 필드들을 자동으로 감지하여 초기화
// 테스트 시작 전 Mock 객체를 생성하고, 테스트 종료 후에는 사용된 Mockito 세션을 해제
@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

  // @Mock: 테스트 시 실제 객체 대신 사용할 가짜 객체(Mock Object)를 생성하는 어노테이션
  @Mock
  private UserRepository userRepository;
  @Mock
  private UserMapper userMapper;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private BinaryContentStorage binaryContentStorage;

  // @InjectMocks: 테스트 대상이 되는 클래스의 인스턴스를 생성하고 그 클래스가 의존하는 Mock 객체들을 자동으로 주입
  @InjectMocks
  private BasicUserService userService;

  // TODO: 반복되는 코드 줄이기
//  private UUID userId;
//  private String username;
//  private String email;
//  private String password;
//  private User user;
//  private UserDto userDto;
//
//  @BeforeEach
//  void setUp() {
//    userId = UUID.randomUUID();
//    username = "kim";
//    email = "kim@example.com";
//    password = "password1234";
//
//    user = new User(username, email, password, null);
//    ReflectionTestUtils.setField(user, "id", userId);
//    userDto = new UserDto(userId, username, email, null, true);
//  }

  // === Helper Method ===

  private User userInit(String username, String email, String password) {
    User user = new User(username, email, password, null);
    // ReflectionTestUtils.setField(대상 객체, 필드명, 넣을 값)
    // 테스트 코드에서 객체의 private 필드나 protected 필드에 값을 직접 주입할 때 사용
    // id는 JPA가 저장 시점 생성하므로 -> 테스트 용으로 id가 있는 엔티티를 생성
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    // UserStatus 생성 및 연결
    UserStatus status = new UserStatus(user, Instant.now());
    user.assignUserStatus(status);
    return user;
  }

  private UserDto dtoFrom(User user) {
    return new UserDto(user.getId(), user.getUsername(), user.getEmail(), null, true);
  }

  // === create() ===
  // 테스트 클래스 내부에 중첩된(Nested) 테스트 클래스를 정의할 때 사용
  // 이를 통해 관련 있는 테스트들을 논리적으로 그룹화하고 계층 구조로 관리
  @Nested
  // 테스트 클래스나 테스트 메서드에 사용자가 정의한 이름을 부여할 때 사용
  @DisplayName("create()")
  class Create {

    // 해당 메서드가 테스트 케이스임을 나타내는 어노테이션
    // 각 @Test 메서드가 호출될 때마다 테스트 클래스의 새로운 인스턴스가 생성
    // 테스트 메서드는 반드시 void 타입이어야 한다
    @Test
    @DisplayName("성공 - 프로필 사진 없이 사용자 생성")
    void create_shouldCreateUser_whenNoProfileProvided() {
      // given
      // 요청 객체 생성 - 서비스에 전달할 회원 생성 요청
      UserCreateRequest request =
          new UserCreateRequest("kim", "kim@example.com", "password1");
      // 저장된 사용자 엔티티 준비 - DB에 저장된 뒤 반환될 User 엔티티를 미리 만들어 두는 것
      // 테스트에서는 DB를 사용하지 않고 userRepository.save()를 mocking하기 때문
      User savedUser = userInit("kim", "kim@example.com", "password1");
      UserDto expectedDto = dtoFrom(savedUser);

      // 이메일 중복 없음
      given(userRepository.existsByEmail("kim@example.com")).willReturn(false);
      // 사용자명 중복 없음
      given(userRepository.existsByUsername("kim")).willReturn(false);
      // 사용자 저장 시 savedUser 반환
      given(userRepository.save(any(User.class))).willReturn(savedUser);
      // User -> UserDto 변환 결과 지정
      given(userMapper.toDto(savedUser)).willReturn(expectedDto);

      // when
      UserDto result = userService.create(request, Optional.empty());

      // then
      // assertThat -> 자바 단위 테스트에서 특정 결과값이 예상과 일치하는지 확인할 때 사용하는 메서드
      // assertThat(결과값).isEqualTo(기대값)

      assertThat(result.username()).isEqualTo("kim");
      assertThat(result.email()).isEqualTo("kim@example.com");
      assertThat(result.profile()).isNull();

      // 프로필 사진이 없다면 파일 메타데이터 + 바이너리 데이터를 저장할 필요가 없음
      // 저장되지 않았는지를 확인하는 부분
      // then(mock).should().save(any());	-> mock은 save 메서드를 호출해야 한다
      // never() -> 호출되지 않았는지
      then(binaryContentRepository).should(never()).save(any());
      then(binaryContentStorage).should(never()).put(any(), any());
    }

    @Test
    @DisplayName("성공 - 프로필 사진 첨부한 사용자 생성")
    void create_shouldCreateUserWithProfile_whenProfileProvided() {
      // given
      UserCreateRequest request =
          new UserCreateRequest("lee", "lee@example.com", "password2");
      BinaryContentCreateRequest profileRequest =
          new BinaryContentCreateRequest("profile.jpg", "image/jpg", "profile".getBytes());

      BinaryContent savedProfile = new BinaryContent("profile.jpg", 7L, "image/jpg");
      ReflectionTestUtils.setField(savedProfile, "id", UUID.randomUUID());

      User savedUser = userInit("lee", "lee@example.com", "password2");
      UserDto expectedDto = dtoFrom(savedUser);

      given(userRepository.existsByEmail("lee@example.com")).willReturn(false);
      given(userRepository.existsByUsername("lee")).willReturn(false);
      given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(savedProfile);
      given(userRepository.save(any(User.class))).willReturn(savedUser);
      given(userMapper.toDto(savedUser)).willReturn(expectedDto);

      // when
      UserDto result = userService.create(request, Optional.of(profileRequest));

      // then
      assertThat(result).isEqualTo(expectedDto);
      then(binaryContentRepository).should().save(any(BinaryContent.class));
      then(binaryContentStorage).should().put(any(), any(byte[].class));
    }

    @Test
    @DisplayName("실패 - 이미 사용 중인 이메일로 생성 시 EmailAlreadyExistsException 발생")
    void create_shouldThrowEmailAlreadyExistsException_whenEmailAlreadyExists() {
      // given
      UserCreateRequest request =
          new UserCreateRequest("jung", "jung@example.com", "password3");

      given(userRepository.existsByEmail("jung@example.com")).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.create(request, Optional.empty()))
          .isInstanceOf(EmailAlreadyExistsException.class);

      then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 이미 사용 중인 사용자명으로 생성 시 UsernameAlreadyExistsException 발생")
    void create_shouldThrowUsernameAlreadyExistsException_whenUsernameAlreadyExists() {
      // given
      UserCreateRequest request =
          new UserCreateRequest("moon", "moon@example.com", "password4");

      given(userRepository.existsByEmail("moon@example.com")).willReturn(false);
      given(userRepository.existsByUsername("moon")).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.create(request, Optional.empty()))
          .isInstanceOf(UsernameAlreadyExistsException.class);

      then(userRepository).should(never()).save(any());
    }
  }

  // === update() ===
  @Nested
  @DisplayName("update()")
  class Update {

    @Test
    @DisplayName("성공 - username, email, password 수정")
    void update_shouldUpdateAllFields_whenValidRequestProvided() {
      // given
      User existingUser =
          userInit("oldName", "oldName@example.com", "oldPassword");
      UUID userId = existingUser.getId();
      UserUpdateRequest request =
          new UserUpdateRequest("newName", "newName@example.com", "newPassword");
      UserDto expectedDto =
          new UserDto(userId, "newName", "newName@example.com", null, null);

      given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
      given(userRepository.existsByEmail("newName@example.com")).willReturn(false);
      given(userRepository.existsByUsername("newName")).willReturn(false);
      given(userMapper.toDto(existingUser)).willReturn(expectedDto);

      // when
      UserDto result = userService.update(userId, request, Optional.empty());

      // then
      Assertions.assertThat(result.username()).isEqualTo("newName");
      Assertions.assertThat(result.email()).isEqualTo("newName@example.com");
      // TODO: password?
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자 수정 시 UserNotFoundException 발생")
    void update_shouldThrowUserNotFoundException_whenUserNotFound() {
      // given
      UUID unknownId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("name", "email@example.com", "password");

      given(userRepository.findById(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.update(unknownId, request, Optional.empty()))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("실패 - 다른 사용자가 이미 사용 중인 이메일로 수정 시 EmailAlreadyExistsException 발생")
    void update_shouldThrowEmailAlreadyExistsException_whenEmailTakenByOther() {
      // given
      User existingUser = userInit("user1", "user1@example.com", "password");
      UUID userId = existingUser.getId();
      UserUpdateRequest request = new UserUpdateRequest("user1", "taken@example.com", null);

      given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
      given(userRepository.existsByEmail("taken@example.com")).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.update(userId, request, Optional.empty()))
          .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("실패 - 다른 사용자가 이미 사용 중인 사용자으로 수정 시 UsernameAlreadyExistsException 발생")
    void update_shouldThrowUsernameAlreadyExistsException_whenUsernameTakenByOther() {
      // given
      User existingUser = userInit("user1", "user1@example.com", "password");
      UUID userId = existingUser.getId();
      UserUpdateRequest request = new UserUpdateRequest("takenUser", "user1@example.com", null);

      given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
      given(userRepository.existsByUsername("takenUser")).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.update(userId, request, Optional.empty()))
          .isInstanceOf(UsernameAlreadyExistsException.class);
    }
  }

  // === delete() ===
  @Nested
  @DisplayName("delete()")
  class Delete {

    @Test
    @DisplayName("성공 - 사용자 삭제 후 repository.delete() 호출 검증")
    void delete_shouldDeleteUser_whenUserExists() {
      // given
      User existingUser = userInit("user", "user@example.com", "password");
      UUID userId = existingUser.getId();

      given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

      // when
      userService.delete(userId);

      // then
      then(userRepository).should().delete(existingUser);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자 삭제 시 UserNotFoundException 발생")
    void delete_shouldThrowUserNotFoundException_whenUserNotFound() {
      // given
      UUID unknownId = UUID.randomUUID();

      given(userRepository.findById(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.delete(unknownId))
          .isInstanceOf(UserNotFoundException.class);

      then(userRepository).should(never()).delete(any());
    }
  }
}