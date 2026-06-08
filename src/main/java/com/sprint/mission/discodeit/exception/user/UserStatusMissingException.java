package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class UserStatusMissingException extends UserException {

  public UserStatusMissingException(Map<String, Object> details) {
    super(ErrorCode.USER_STATUS_MISSING, details);
  }
}
