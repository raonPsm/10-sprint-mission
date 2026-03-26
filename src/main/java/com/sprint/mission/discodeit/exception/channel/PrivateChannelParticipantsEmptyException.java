package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class PrivateChannelParticipantsEmptyException extends ChannelException {

  public PrivateChannelParticipantsEmptyException(Map<String, Object> details) {
    super(ErrorCode.PRIVATE_CHANNEL_PARTICIPANTS_EMPTY, details);
  }
}
