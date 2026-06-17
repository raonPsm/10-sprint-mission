package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.SseService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 도메인 이벤트를 SSE 전송으로 변환하는 리스너. 서비스 계층과 SSE 전송 관심사 분리
@Slf4j
@Component
@RequiredArgsConstructor
public class SseEventListener {

  private final SseService sseService;

  // 새로운 알림 이벤트 전송 처리
  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(NotificationCreatedEvent event) {
    NotificationDto dto = event.notification();
    sseService.send(List.of(dto.receiverId()), "notifications.created", dto);
  }

  // 파일 업로드 상태 변경 이벤트 전송 처리
  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentStatusChangedEvent event) {
    sseService.broadcast("binaryContents.updated", event.binaryContent());
  }

  // 채널 갱신 이벤트 전송 처리
  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelChangedEvent event) {
    ChannelDto dto = event.channel();
    String eventName = switch (event.type()) {
      case CREATED -> "channels.created";
      case UPDATED -> "channels.updated";
      case DELETED -> "channels.deleted";
    };
    // PRIVATE 채널은 참여자에게만, PUBLIC 채널은 전체 broadcast
    if (dto.type() == ChannelType.PRIVATE) {
      List<UUID> receiverIds = dto.participants().stream().map(UserDto::id).toList();
      sseService.send(receiverIds, eventName, dto);
    } else {
      sseService.broadcast(eventName, dto);
    }
  }

  // 사용자 생성/수정/삭제(CREATED/UPDATED/DELETED) 이벤트 처리
  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserChangedEvent event) {
    if (event.type() == UserChangedEvent.Type.STATUS_CHANGED) {
      return; // STATUS_CHANGED는 onUserStatusChanged에서 처리
    }
    broadcastUserEvent(event);
  }

  // 사용자 상태 변경(STATUS_CHANGED: 로그인/로그아웃) 이벤트 처리
  @Async("eventTaskExecutor")
  @EventListener
  public void onUserStatusChanged(UserChangedEvent event) {
    if (event.type() != UserChangedEvent.Type.STATUS_CHANGED) {
      return;
    }
    broadcastUserEvent(event);
  }

  private void broadcastUserEvent(UserChangedEvent event) {
    String eventName = switch (event.type()) {
      case CREATED -> "users.created";
      case UPDATED, STATUS_CHANGED -> "users.updated";
      case DELETED -> "users.deleted";
    };
    sseService.broadcast(eventName, event.user());
  }
}
