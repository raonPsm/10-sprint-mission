package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaAuditingTestConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/*
JPA 관련 레이어만 테스트 하기 위해 사용하는 슬라이스 테스트 어노페이션
- 기본적으로 모든 테스트 메서드에 트랜잭션을 적용하며, 테스트가 끝난 후 데이터를 롤백하여 다음 테스트에 영향을 주지 않도록 한다.
 */
@DataJpaTest
// Spring의 test 프로필을 활성화 <- application-test.yaml 파일의 설정값을 읽어와 테스트 환경을 구성
@ActiveProfiles("test")
// 테스트 시 자동으로 설정되는 내장 데이터베이스(H2)로 교체하지 않겠다는 의미
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @DataJpaTest는 기본적으로 JPA Auditing 관련 빈을 스캔하지 않기 때문에,
// 이를 활성화하기 위한 설정 클래스를 수동으로 임포트해 주는 것
@Import(JpaAuditingTestConfig.class)
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  // JPA 엔티티를 테스트할 때  EntityManager 대신 사용할 수 있도록 제공되는 테스트 전용 헬퍼 클래스
  // repository를 거치지 않고 테스트 데이터를 세팅할 때 유용
  @Autowired
  private TestEntityManager em;

  // === Helper Method ===
  private User persistUser(String username, String email) {
    User user = new User(username, email, "password", null);

    em.persist(user); // user 객체를 영속성 컨텍스트(1차 캐시)에 등록 / Transient -> Managed / DB 쿼리 X
    em.flush(); // 영속성 컨텍스트의 변경사항을 즉시 DB에 동기화 / DB 쿼리 O / 1차 캐시는 유지
    em.clear(); // 영속성 컨텍스트를 완전히 비움 / Managed -> Detached
    // 1차 캐시를 비우지 않으면 실제로 DB에 SELECT 쿼리가 나가지 않으므로 실제 쿼리 검증이 불가능

    return user;
  }

  private User persistUserWithStatus(String username, String email) {
    User user = new User(username, email, "password", null);
    UserStatus userStatus = new UserStatus(user, Instant.now());

    em.persist(user);
    em.persist(userStatus);
    em.flush();
    em.clear();

    return user;
  }

  // boolean existsByUsername(String username);
  @Nested
  @DisplayName("existsByUsername")
  class ExistsByUsername {

    @Test
    @DisplayName("성공 - 존재하는 username이면 true 반환")
    void existsByUsername_exists_returnsTrue() {
      // given
      persistUser("kim", "kim@example.com");

      // when & then
      assertThat(userRepository.existsByUsername("kim")).isTrue();
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 username이면 false 반환")
    void existsByUsername_notExists_returnFalse() {
      // given

      // when & then
      assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }
  }

  // boolean existsByEmail(String email);
  @Nested
  @DisplayName("existsByEmail")
  class ExistsByEmail {

    @Test
    @DisplayName("성공 - 존재하는 email이면 true 반환")
    void existByEmail_exists_returnsTrue() {
      // given
      persistUser("testuser", "test@example.com");

      // when & then
      assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 email이면 false 반환")
    void existsByEmail_notExists_returnsFalse() {
      // given

      // when & then
      assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }
  }

  // Optional<User> findByUsername(String username);
  @Nested
  @DisplayName("findByUsername")
  class FindByUsername {

    @Test
    @DisplayName("성공 - 존재하는 username으로 조회 시 User 반환")
    void findByUsername_found() {
      // given
      persistUser("kim", "kim@example.com");

      // when
      Optional<User> result = userRepository.findByUsername("kim");

      // then
      assertThat(result).isPresent();
      assertThat(result.get().getUsername()).isEqualTo("kim");
      assertThat(result.get().getEmail()).isEqualTo("kim@example.com");
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 username이면 empty 반환")
    void findByUsername_notFound() {
      // given

      // when
      Optional<User> result = userRepository.findByUsername("nobody");

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAllWithProfileAndUserStatus")
  class FindAllWithProfileAndUserStatus {

    @Test
    @DisplayName("성공 - User 목록과 연관 데이터를 함께 반환")
    void findAllWithProfileAndUserStatus_returnAll() {
      // given
      persistUserWithStatus("kim", "kim@example.com");
      persistUserWithStatus("lee", "lee@example.com");

      // when
      List<User> users = userRepository.findAllWithProfileAndUserStatus();

      // then
      assertThat(users).hasSize(2);
      assertThat(users).extracting(User::getUsername)
          .containsExactlyInAnyOrder("kim", "lee");
    }

    @Test
    @DisplayName("성공 - User가 없을 때 빈 리스트 반환")
    void findAllWithProfileAndUserStatus_empty() {
      // given

      // when
      List<User> users = userRepository.findAllWithProfileAndUserStatus();

      // then
      assertThat(users).isEmpty();
    }
  }
}
