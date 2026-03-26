package com.sprint.mission.discodeit.dto.requestRespose.error;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * API 예외 발생 시 클라이언트에게 반환하는 통일된 에러 응답 형식
 */
public record ErrorResponse(
    Instant timestamp,            // 예외가 발생한 시각
    String code,                  // ErrorCode Enum 이름
    String message,               // 에러 메시지
    Map<String, String> details,  // 예외 발생 상황에 대한 추가 정보
    String exceptionType,         // 발생한 예외의 클래스 이름
    int status                    // HTTP 상태 코드
) {

  // 커스텀 예외가 아닌 일반 예외 처리시 사용하는 메서드
  public static ErrorResponse of(HttpStatus httpStatus, String message, String exceptionType) {
    return new ErrorResponse(
        Instant.now(),
        null, // ErrorCode가 없으므로 code는 null
        message,
        Map.of(), // 추가 정보 없음 -> 빈 Map
        exceptionType,
        httpStatus.value()
    );
  }
}