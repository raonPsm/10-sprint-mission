package com.sprint.mission.discodeit.controller.exception;

import com.sprint.mission.discodeit.dto.requestRespose.error.ErrorResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
// @RestController에서 발생하는 예외를 한곳에서 잡아 처리
// @ControllerAdvice + @ResponseBody가 합쳐진 형태
// 예외 처리 후 객체를 바로 JSON/XML 형태로 반환할 수 있다.
@Slf4j
public class GlobalExceptionHandler {

  // 커스텀 예외 처리
  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDigestException(DiscodeitException ex) {
    HttpStatus status = ex.getErrorCode().getHttpStatus();
    log.warn("[{}] {}: {}",
        ex.getErrorCode().name(),
        ex.getClass().getSimpleName(),
        ex.getDetails()
    );
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(
            ex.getTimestamp(),             // timestamp
            ex.getErrorCode(),             // errorCode
            ex.getDetails(),               // details
            ex.getClass().getSimpleName(), // exceptionType
            status                         // httpStatus
        ));
  }

  // 유효성 검증 실패 (@Valid)
  // MethodArgumentNotValidException : Spring MVC에서 @Vaild, @Vaildated 을 사용한
  // 객체의 유효성 검증이 실패했을 때 발생하는 예외. 기본적으로 400 Bad Request 응답을 반환
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    // BindingResult : 스프링 프레임워크에서 객체 바인딩 및 검증 과정에서 발생하는 오류를 보관하고 관리하는 인터페이스
    // FieldError -> 특정 필드에서 발생한 검증 오류
    Map<String, Object> details = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            // keyMapper
            FieldError::getField,
            // valueMapper
            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값입니다.",
            // mergeFunction - key 충돌 시 처리하는 함수 / 첫 오류로 반환
            (existing, duplicate) -> existing
        ));
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(
            "입력값 유효성 검증 실패",      // message
            details,                            // details
            "MethodArgumentNotValidException",  // exceptionType TODO: ErrorCode에 추가?
            HttpStatus.BAD_REQUEST              // httpStatus
        ));
  }

  // 처리하지 않은 모든 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    log.error("[UNHANDLED_EXCEPTION] {}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(
            ex.getMessage(),                   // message
            ex.getClass().getSimpleName(),     // exceptionType
            HttpStatus.INTERNAL_SERVER_ERROR   // httpStatus
        ));
  }
}
