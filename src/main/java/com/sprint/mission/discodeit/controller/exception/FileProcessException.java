package com.sprint.mission.discodeit.controller.exception;

/**
 * 파일 업로드 및 처리 중 발생하는 예외를 정의합니다.
 */
public class FileProcessException extends RuntimeException {
    public FileProcessException(String message) {
        super(message);
    }

    public FileProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}