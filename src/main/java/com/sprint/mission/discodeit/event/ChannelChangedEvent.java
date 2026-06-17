package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.data.ChannelDto;

// 채널 갱신 이벤트
public record ChannelChangedEvent(
    Type type,
    ChannelDto channel
) {

  public enum Type {
    CREATED, UPDATED, DELETED
  }
}
