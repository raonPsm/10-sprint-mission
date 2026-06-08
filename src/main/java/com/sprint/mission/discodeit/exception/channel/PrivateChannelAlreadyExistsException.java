package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class PrivateChannelAlreadyExistsException extends ChannelException {

  public PrivateChannelAlreadyExistsException(Map<String, Object> details) {
    super(ErrorCode.PRIVATE_CHANNEL_ALREADY_EXISTS, details);
  }
}
