package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.SseApi;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.SseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// SSE(Server-Sent Events) 연결을 관리하는 컨틀롤러
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sse")
public class SseController implements SseApi {

  private final SseService sseService;

  // SSE 연결을 수립하고 SseEmitter를 반환
  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE) // 브라우저가 이 응답을 SSE 스트림으로 인식하도록
  @Override
  public SseEmitter connect(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails,
      // EventSource를 지원하지 않는 환경에서는 Polyfill 라이브러리를 사용. Polyfill은 헤더 대신 쿼리 파라미터로 전달
      @RequestParam(value = "lastEventId", required = false) String lastEventId,
      // 표준 브라우저 SSE 헤더. 브라우저 내장 EventSource가 재연결 시 자동으로 헤더를 붙여준다.
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventIdHeader) {
    UUID receiverId = userDetails.getUserDto().id(); // 인증 정보에서 수신자 ID 추출
    // 쿼리 파라미터 우선, 없으면 표준 헤더 사용
    UUID resolvedLastEventId = parseUuid(lastEventId != null ? lastEventId : lastEventIdHeader);
    return sseService.connect(receiverId, resolvedLastEventId);
  }

  // 잘못된 형식이 와도 재연결이 끊기지 않도록 방어적으로 파싱
  private UUID parseUuid(String value) {
    if (value == null || value.isBlank()) { // null 또는 빈 값이면 null 반환
      return null;
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      return null; // 파싱 실패 시 null 반환
    }
  }
}
