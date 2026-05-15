package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
// ApplicationRunner
//   애플리케이션 구동이 완료된 직후(SpringApplication.run(...) 메서드가 끝나기 직전)에
//   특정 로직을 자동으로 실행하고 싶을 때 사용하는 인터페이스
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final PasswordEncoder passwordEncoder;
  private final AdminProperties adminProperties;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (userRepository.existsByRole(UserRole.ADMIN)) {
      log.info("Admin 계정이 이미 존재합니다. 초기화를 건너뜁니다.");
      return;
    }

    String encodedPassword = passwordEncoder.encode(adminProperties.password());
    User admin = new User(
        adminProperties.username(),
        adminProperties.email(),
        encodedPassword,
        null,
        UserRole.ADMIN
    );
    userRepository.save(admin); // 초기화 로직이므로 repo 계층 직접 접근

    UserStatus status = new UserStatus(admin, Instant.now());
    userStatusRepository.save(status);

    log.info("Admin 계정 생성 완료: username={}", adminProperties.username());
  }
}
