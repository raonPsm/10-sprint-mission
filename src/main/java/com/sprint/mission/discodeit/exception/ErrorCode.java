package com.sprint.mission.discodeit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// FIXME(Later): 사용자 or 유저 이름 통일 할 것 -> log랑 맞출 것
@Getter
@AllArgsConstructor
public enum ErrorCode {
  // User
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
  USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용중인 유저 이름입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
  USER_STATUS_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "유저 상태 데이터가 누락되었습니다."),

  // Channel
  CHANNEL_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 공개 채널 이릅입니다."),
  PRIVATE_CHANNEL_PARTICIPANTS_EMPTY(HttpStatus.BAD_REQUEST, "비공개 채널 참여자가 지정되지 않았습니다."),
  PRIVATE_CHANNEL_ALREADY_EXISTS(HttpStatus.CONFLICT, "동일한 참여자 구성의 비공개 채널이 이미 존재합니다."),
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "비공개 채널은 수정할 수 없습니다."),

  // Message
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),

  // BinaryContent
  BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."),
  FILE_PROCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다."),

  // Auth
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),

  // UserStatus
  USER_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 유저의 상태 정보가 존재합니다."),
  USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 상태를 찾을 수 없습니다."),

  // ReadStatus
  READ_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 유저의 읽기 상태가 존재합니다."),
  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "읽기 상태를 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
