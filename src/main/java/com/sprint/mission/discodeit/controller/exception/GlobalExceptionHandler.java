package com.sprint.mission.discodeit.controller.exception;

import com.sprint.mission.discodeit.dto.requestRespose.error.ErrorResponse;
import java.util.NoSuchElementException;
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

  // IllegalArgumentException (잘못된 인자 전달)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    // 400 Bad Request
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(e.getMessage(), "IllegalArgumentException"));
  }

  // NoSuchElementException (데이터가 없을 때)
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
    // 404 Not Found
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(e.getMessage(), "NoSuchElementException"));
  }

  @ExceptionHandler(FileProcessException.class)
  public ResponseEntity<ErrorResponse> handleFileProcessException(FileProcessException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(e.getMessage(), "FileProcessException"));
  }

  // 처리하지 않은 모든 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
    log.error("[UNHANDLED_EXCEPTION] {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage(),
                e.getClass().getSimpleName()
            )
        );
  }
}
