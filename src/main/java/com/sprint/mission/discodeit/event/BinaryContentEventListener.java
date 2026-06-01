package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;

  /* 현재 실행 중인 트랜잭션이 성공적으로 커밋된 직후에 이벤트를 처리하도록 함
  AFTER_COMMIT을 통해 DB 처리가 확정된 뒤에 파일 관련 IO 작업 진행
  
  <동작 순서>
  1 서비스 메서드 시작 (@Transactional)
  2 DB 저장 binaryContentRepository.save(binaryContent)
  3 publishEvent(new BinaryContentCreatedEvent(binaryContent.getId(), bytes))
    Spring이 이벤트 객체를 내부에 보관
  4 트랜잭션 커밋
  5 Spring이 꺼내서 리스너 실행 -> handleBinaryContentCreated(BinaryContentCreatedEvent event) 호출
  6 binaryContentStorage.put(event.binaryContentId(), event.bytes());

  https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html
  https://docs.spring.io/spring-modulith/reference/events.html
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBinaryContentCreated(BinaryContentCreatedEvent event) {
    log.debug("바이너리 컨텐츠 파일 저장 시작: id={}", event.binaryContentId());
    binaryContentStorage.put(event.binaryContentId(), event.bytes());
    log.debug("바이너리 컨텐츠 파일 저장 완료: id={}", event.binaryContentId());
  }
}
