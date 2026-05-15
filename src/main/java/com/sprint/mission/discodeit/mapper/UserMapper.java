package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public abstract class UserMapper { // SessionRegistry 주입받기 위해 abstract class

  @Autowired
  protected SessionRegistry sessionRegistry;

  @Mapping(target = "online", expression = "java(isOnline(user.getId()))")
  public abstract UserDto toDto(User user);

  protected boolean isOnline(UUID userId) {
    return sessionRegistry.getAllPrincipals().stream() // 현재 로그인된 모든 사용자 가져와서
        .filter(p -> p instanceof DiscodeitUserDetails) // DiscodeitUserDetails 타입만 필터링
        .map(p -> (DiscodeitUserDetails) p) // Object -> DiscodeitUserDetails로 캐스팅
        .filter(ud -> ud.getUserDto().id().equals(userId)) // 찾으려는 userId와 일치하는 사용자 필터링
        // 해당 사용자의 만료되지 않은 세션이 있는지 확인
        .anyMatch(ud -> !sessionRegistry.getAllSessions(ud, false).isEmpty());
  }
}
