package com.sprint.mission.discodeit.dto.sse;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

// SSE로 전송되는 단일 메시지
// 각 메시지는 고유한 이벤트 ID를 가지며, 클라이언트 재연결 시 보내는 LastEventId 기반의 유실 복원에 사용됨
// 연결이 끊겼다 다시 붙은 클라이언트가 마지막으로 받은 이벤트 id를 서버에 알려주면, 서버는 그 id 이후에 발생한 메시지들을 찾아 다시 보내준다.
public record SseMessage(
    UUID id, // 이벤트 고유 식별자. 저장소에 저장될 때 부여됨
    String eventName, // 이벤트 종류 이름. 클라이언트가 어떤 이벤트인지 구분하는 데 사용
    Object data, // 실제 전송할 내용
    Set<UUID> receiverIds // 메시지를 받을 수신자 UUID Set. 비어 있으면 전체 broadcast
) {

  // 특정 사용자들을 대상으로 하는 메시지를 생성 (id는 저장 시점에 부여)
  public static SseMessage of(String eventName, Object data, Collection<UUID> receiverIds) {
    // null이면 빈 집합, 아니면 불변 복사본 생성
    Set<UUID> targets = (receiverIds == null) ? Set.of() : Set.copyOf(receiverIds);
    return new SseMessage(null, eventName, data, targets);
  }

  // 모든 연결을 대상으로 하는 broadcast 메시지를 생성 (id는 저장 시점에 부여)
  public static SseMessage broadcast(String eventName, Object data) {
    return new SseMessage(null, eventName, data, Set.of());
  }

  // 동일한 내용에 id만 부여한 복사본을 반환 (불변성 유지)
  public SseMessage withId(UUID newId) {
    return new SseMessage(newId, eventName, data, receiverIds);
  }

  // 이 메시지가 전체 broadcast 대상인지 여부 확인
  public boolean isBroadcast() {
    return receiverIds == null || receiverIds.isEmpty();
  }

  // 해당 메시지가 주어진 사용자에게 전달되어야 하는지 여부. (broadcast이거나 대상에 포함된 경우)
  public boolean isTargetedTo(UUID receiverId) {
    return isBroadcast() || receiverIds.contains(receiverId);
  }
}
