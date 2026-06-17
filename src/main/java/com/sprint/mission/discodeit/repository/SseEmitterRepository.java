package com.sprint.mission.discodeit.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/*
SseEmitter 객체들을 메모리에 보관/조회/삭제하는 저장소 컴포넌트

사용자(UUID)당 여러 개의 연결(Ex-다중 탭)을 허용하기 위해 List<SseEmitter>로 관리
순회 중 콜백에 의한 동시 제거가 안전하도록 값은 CopyOnWriteArrayList를 사용한다.
 */
@Repository
public class SseEmitterRepository {

  // Key: 수신자(사용자) UUID / Value: 해당 사용자의 활성 SSE 연결 목록
  // ConcurrentHashMap -> 여러 유저의 동시 접속/해제로 Map 동시 접근 시 데이터 깨짐 방지
  private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

  // 특정 수신자의 SSE 연결을 저장
  // computeIfAbsent(): 지정된 키가 없거나 null일 때만 새로운 값을 계산하여 Map에 추가
  // -> 키 단위 락으로 Map 자체의 동시 접근 안전 보장
  // CopyOnWriteArrayList: 멀티스레드 환경에서 안전하게 사용할 수 있는 thread-safe한 List 구현체
  //                       내부 데이터를 수정할 때마다 기존 배열을 복사하여 새로운 배열을 만드는 방식으로 동작
  // -> 리스트 순회 중 수정 안전 보장
  // (이벤트를 발송하려고 연결 목록을 순회하는 도중, 마침 어떤 연결이 종료되면서 그 목록이 수정되어 발송 작업 전체가 깨지는 것 방지)
  // SSE 특성상 아래 두 동작이 동시에 발생
  //   스레드A: 리스트 순회하며 이벤트 전송 (읽기)
  //   스레드B: 연결 종료 시 emitter 제거 (쓰기)
  public SseEmitter save(UUID receiverId, SseEmitter emitter) {
    data.computeIfAbsent(receiverId, key -> new CopyOnWriteArrayList<>()).add(emitter); // 원자성 보장
    return emitter;
  }

  // 특정 수신자에게 연결된 모든 SseEmitter 목록을 조회
  public List<SseEmitter> findByReceiverId(UUID receiverId) {
    return data.getOrDefault(receiverId, List.of());
    // default V getOrDefault(Object key, V defaultValue)
    //   키가 존재하면 -> 해당 값 반환
    //   키가 없으면 -> defaultValue 반환
  }

  // 특정 수신자의 SSE 연결 목록에서 지정한 SseEmitter 하나를 제거
  public void deleteByReceiverIdAndEmitter(UUID receiverId, SseEmitter emitter) {
    List<SseEmitter> emitters = data.get(receiverId); // 해당 사용자의 연결 목록 꺼내서
    if (emitters != null) {
      emitters.remove(emitter); // 지정한 emitter 하나 제거
      if (emitters.isEmpty()) { // 제거하고 나서 리스트가 비었는지 확인 (= 이 사용자의 마지막 연결이 끊겼는지 확인)
        // 사용자의 마지막 연결이 끊겼으면 더 이상 Map에 이 사용자를 둘 필요가 없으므로 제거
        // remove(key, value): 키의 '현재 값'이 value와 같을 때만 제거 (동시성 문제로 인해 인자 2개 remove 사용)
        data.remove(receiverId, emitters);
      }
    }
  }

  // 전체 수신자 SseEmitter 반환
  public Map<UUID, List<SseEmitter>> getAll() {
    return data;
  }

  // 활성 연결이 존재하는 모든 수신자의 UUID를 반환
  // keySet(): Map에 포함된 모든 Key를 Set 객체로 반환
  public Set<UUID> getAllReceiverIds() {
    return data.keySet();
  }
}
