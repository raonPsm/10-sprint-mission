package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class FileProcessException extends BinaryContentException {

  public FileProcessException(Map<String, Object> details, Throwable cause) { // 실제 원인 받아서
    super(ErrorCode.FILE_PROCESS_ERROR, details);
    initCause(cause); // 실제 원인 예외 연결
  }
}
