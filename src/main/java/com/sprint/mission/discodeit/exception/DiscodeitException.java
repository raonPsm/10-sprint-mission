package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
public class DiscodeitException extends RuntimeException {

  private final Instant timestamp; // 예외 발생 시각 (자동 기록)
  private final ErrorCode errorCode; // HTTP 상태 + 메시지 정의
  private final Map<String, Object> details; // 예외 발생 상황에 대한 추가정보를 저장하기 위한 속성

  public DiscodeitException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode.getMessage());
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.details = details != null ? details : Map.of(); // NPE 방지 -> 빈 Map 기본값
  }
}
