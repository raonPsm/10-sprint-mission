package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 사용자별 SseEmitter를 생성하고 이벤트를 전송하는 컴포넌트.
@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

  // cleanUp 주기(30분)보다 길게 잡아, 죽은 연결은 ping으로 먼저 정리되도록 함. 타임아웃 시 클라이언트가 재연결.
  private static final long TIMEOUT = 60L * 60 * 1000;

  // receiverId -> SseEmitter 목록 매핑을 관리하는 저장소
  private final SseEmitterRepository sseEmitterRepository;
  // 전송된 SSE 메시지를 보관하는 저장소. 재연결 시 lastEventId 이후의 유실 메시지를 복원하는 데 사용
  private final SseMessageRepository sseMessageRepository;

  // SseEmitter를 생성하고 저장한다. lastEventId가 있으면 유실된 이벤트를 재전송한다.
  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(TIMEOUT); // TIMEOUT 이후 자동 종료되는 SseEmitter 생성
    sseEmitterRepository.save(receiverId, emitter); // emitter를 저장소에 등록

    // 연결 완료 저장소에서 제거
    // onCompletion(): SSE 연결이 종료되었을 때 실행할 콜백 메서드를 등록해두는 메서드
    emitter.onCompletion(() -> {
      log.info("SSE 연결 완료 (receiverId={})", receiverId);
      sseEmitterRepository.deleteByReceiverIdAndEmitter(receiverId, emitter);
    });
    // 타임아웃 시 complete() 호출 -> onCompletion으로 위임
    // onTimeout 콜백 실행 - emitter.complete() 호출
    //   -> complete()가 연결을 완료 상태로 만듦
    //     -> onCompletion 콜백 자동 실행 - 저장소에서 emitter 제거
    emitter.onTimeout(emitter::complete);
    // 에러 시 저장소에서 제거
    emitter.onError(e -> sseEmitterRepository.deleteByReceiverIdAndEmitter(receiverId, emitter));

    // 최초 연결 확인용 더미 이벤트 전송
    try {
      emitter.send(SseEmitter.event()
          .id(UUID.randomUUID().toString())
          .name("sse.connected")
          .data("connected: " + receiverId));
    } catch (IOException e) {
      // 전송 실패 시 저장소에서 제거
      sseEmitterRepository.deleteByReceiverIdAndEmitter(receiverId, emitter);
    }

    // 재연결인 경우 lastEventId 이후 유실된 메시지 복원
    if (lastEventId != null) {
      sseMessageRepository.findAllAfter(lastEventId).stream()
          // 이 수신자 대상 메시지만 필터링
          .filter(message -> message.isTargetedTo(receiverId))
          // 순서대로 재전송
          .forEach(message -> sendToEmitter(receiverId, emitter, message));
    }

    return emitter;
  }

  // 특정 사용자들에게 이벤트를 전송
  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    // 메시지 저장
    SseMessage message = sseMessageRepository.save(SseMessage.of(eventName, data, receiverIds));
    // 대상 수신자 순회
    for (UUID receiverId : receiverIds) {
      // 해당 수신자의 모든 emitter 순회 (다중 탭/기기)
      for (SseEmitter emitter : sseEmitterRepository.findByReceiverId(receiverId)) {
        sendToEmitter(receiverId, emitter, message); // 각 emitter에 메시지 전송
      }
    }
  }

  // 모든 연결에 이벤트를 전송
  public void broadcast(String eventName, Object data) {
    SseMessage message = sseMessageRepository.save(SseMessage.broadcast(eventName, data));
    for (UUID receiverId : sseEmitterRepository.getAllReceiverIds()) {
      for (SseEmitter emitter : sseEmitterRepository.findByReceiverId(receiverId)) {
        sendToEmitter(receiverId, emitter, message);
      }
    }
  }

  // 하나의 emitter에 하나의 메시지를 SSE 이벤트로 내보냄
  private void sendToEmitter(UUID receiverId, SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event()
          .id(message.id().toString()) // 클라이언트가 Last-Event-ID로 기억할 메시지 ID
          .name(message.eventName()) // 프론트 addEventListener 구독 키
          .data(message.data(), MediaType.APPLICATION_JSON)); // 본문 JSON 직렬화 후 전송
    } catch (IOException | IllegalStateException e) {
      // 죽은 클라이언트 혹은 이미 완료된 emitter -> 저장소에서 제거
      sseEmitterRepository.deleteByReceiverIdAndEmitter(receiverId, emitter);
    }
  }

  // 주기적으로 ping을 보내 만료된 SseEmitter를 삭제한다.
  @Scheduled(fixedDelay = 1000 * 60 * 30) // 이전 실행 완료 후 30분마다 실행
  public void cleanUp() {
    sseEmitterRepository.getAll().forEach((receiverId, emitters) -> {
      for (SseEmitter emitter : emitters) {
        if (!ping(emitter)) { // ping 실패 시 죽은 연결로 판단
          sseEmitterRepository.deleteByReceiverIdAndEmitter(receiverId, emitter); // 저장소에서 제거
        }
      }
    });
  }

  // 최초 연결 또는 만료 여부 확인용 더미 이벤트(주석 프레임)를 보냄
  private boolean ping(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().comment("keep-alive")); // 주석 보내서 연결 생존 확인
      return true; // 전송 성공 -> 살아 있음
    } catch (IOException | IllegalStateException e) {
      emitter.complete(); // 죽은 연결 정리
      return false;
    }
  }
}
