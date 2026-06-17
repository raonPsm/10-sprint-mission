package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

/*
SSE로 발행한 메시지를 메모리에 잠시 보관하는 저장소
클라이언트 연결이 잠깐 끊겼다가 재연결될 때, 끊긴 사이에 발행된 메시지를 다시 재전송하여 이벤트 유실을 복원하는 것이 목적

# SSE의 유실 복원 매커니즘
- 서버는 각 이벤트에 고유 ID를 실어 보낸다
- 클라이언트(브라우저 EventSource)는 마지막으로 받은 ID를 기억하고 있다가,
  재연결 시 'Last-Event-Id' 헤더로 그 값을 다시 보낸다
- 서버는 그 ID 이후의 메시지들만 골라 재전송하면 끊긴 구간이 메워진다.

- 발행 순서를 보존해야 하므로 큐에 ID를 순서대로 쌓는다
- ID로 실제 메시지를 빠르게 찾아야 하므로 Map(ID -> 메시지)을 함께 둔다.
  => 큐는 '순서', 맵은 '조회'를 담당하는 이중 자료구조 작성
- 메모리에 무한정 쌓이면 누수가 되므로 최대 MAX_SIZE개로 제한 (오래된 것부터 제거)
 */
@Repository
public class SseMessageRepository {

  // 메모리에 보관할 최대 메시지 개수. 초과하면 가장 오래된 것부터 버린다.
  private static final int MAX_SIZE = 1000;

  // 발행된 메시지 ID를 발행 순서대로 보관하는 큐
  // ConcurrentLinkedDeque: 락 없이 여러 스레드가 동시에 앞/뒤를 넣고 빼도 안전
  //   - addLast(): 새 메시지를 맨 뒤에 추가 (가장 최신)
  //   - pollFirst(): 맨 앞(가장 오래된) 것을 꺼내 제거 -> FIFO 정리에 사용
  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  // 메시지 ID -> 실제 메시지 본문 매핑
  // ConcurrentHashMap: 동시 put/get/remove 상황에서 Map 구조가 깨지지 않도록 보장
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  // 메시지에 고유 ID를 부여해 저장하고, 저장본을 반환
  public SseMessage save(SseMessage message) {
    SseMessage stored = message.withId(UUID.randomUUID());
    messages.put(stored.id(), stored); // 조회용 저장
    eventIdQueue.addLast(stored.id()); // 발행 순서 보존용 저장
    evictOld(); // 오래된 메시지 제거
    return stored;
  }

  // 주어진 lastEventId 이후에 발행된 메시지들을 발행 순서대로 반환한다.
  public List<SseMessage> findAllAfter(UUID lastEventId) {
    List<UUID> ids = new ArrayList<>(eventIdQueue); // 현재 큐를 리스트로 복사
    int index = ids.indexOf(lastEventId); // lastEventId의 위치 탐색
    if (index < 0) { // 못 찾으면 (-1)
      return List.of(); // 빈 리스트 반환
    }
    return ids.subList(index + 1, ids.size()).stream() // lastEventId 이후 ID 목록 추출
        .map(messages::get) // ID -> 실제 메시지 변환
        .filter(Objects::nonNull) // Map에서 이미 제거된 메시지 제외
        .toList();
  }

  // 최대 보관 개수를 초과하면 가장 오래된 메시지를 큐/맵에서 함께 제거한다. (FIFO)
  public void evictOld() {
    while (eventIdQueue.size() > MAX_SIZE) {
      UUID oldest = eventIdQueue.pollFirst(); // 가장 오래된 ID를 큐에서 꺼냄
      if (oldest != null) { // 동일 ID를 Map에서도 제거 (상태 일치)
        messages.remove(oldest);
      }
    }
  }
}
