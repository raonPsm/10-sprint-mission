package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class ChannelNameAlreadyExistsException extends ChannelException {

  public ChannelNameAlreadyExistsException(Map<String, Object> details) {
    super(ErrorCode.CHANNEL_NAME_ALREADY_EXISTS, details);
  }
}
