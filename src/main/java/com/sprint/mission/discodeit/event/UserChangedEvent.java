package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.data.UserDto;

// 사용자 갱신 이벤트
public record UserChangedEvent(
    Type type,
    UserDto user
) {

  public enum Type {
    CREATED, UPDATED, DELETED, STATUS_CHANGED
  }
}
