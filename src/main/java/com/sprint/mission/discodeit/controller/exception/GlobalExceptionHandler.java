package com.sprint.mission.discodeit.controller.exception;

import com.sprint.mission.discodeit.dto.requestRespose.error.ErrorResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import java.security.DigestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
// @RestController에서 발생하는 예외를 한곳에서 잡아 처리
// @ControllerAdvice + @ResponseBody가 합쳐진 형태
// 예외 처리 후 객체를 바로 JSON/XML 형태로 반환할 수 있다.
@Slf4j
public class GlobalExceptionHandler {

  // 커스텀 예외 처리
  @ExceptionHandler(DigestException.class)
  public ResponseEntity<ErrorResponse> handleDigestException(DiscodeitException e) {
    HttpStatus status = e.getErrorCode().getHttpStatus();
    log.warn("[{}] {}: {}",
        e.getErrorCode().name(),
        e.getClass().getSimpleName(),
        e.getDetails()
    );
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(
            e.getTimestamp(),             // timestamp
            e.getErrorCode(),             // errorCode
            status,                       // httpStatus
            e.getClass().getSimpleName(), // exceptionType
            e.getDetails()                 // details
        ));
  }

  // 처리하지 않은 모든 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
    log.error("[UNHANDLED_EXCEPTION] {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(
            e.getMessage(),                   // message
            e.getClass().getSimpleName(),     // exceptionType
            HttpStatus.INTERNAL_SERVER_ERROR  // httpStatus
        ));
  }
}
