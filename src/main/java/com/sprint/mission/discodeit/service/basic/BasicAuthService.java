package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.dto.requestRespose.auth.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.auth.InvalidCredentialsException;
import com.sprint.mission.discodeit.exception.user.UserStatusMissingException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  // 연관관계 탐색을 활용하므로 UserStatusRepository 의존성 제거

  @Transactional
  @Override
  public UserDto login(LoginRequest request) {
    User user = userRepository.findByUsername(request.username())
        .filter(u -> u.getPassword().equals(request.password()))
        .orElseThrow(() -> new InvalidCredentialsException(Map.of("username", request.username())));

    UserStatus userStatus = user.getUserStatus();
    if (userStatus == null) {
      throw new UserStatusMissingException(Map.of("userId", user.getId()));
    }

    userStatus.update(Instant.now());

    return userMapper.toDto(user);
  }
}
