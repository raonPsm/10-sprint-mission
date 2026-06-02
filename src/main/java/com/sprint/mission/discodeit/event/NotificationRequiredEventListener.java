package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationService notificationService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void on(MessageCreatedEvent event) {
    String channelDisplay = event.channelName() != null
        ? "#" + event.channelName() : "비공개 채널";
    String title = event.authorUsername() + " (#" + channelDisplay + ")";

    readStatusRepository
        // 해당 채널에서 notificationEnabled=true인 ReadStatus 목록 조회
        .findAllByChannelIdAndNotificationEnabled(event.channelId())
        .stream()
        // 작성자 본인 제외
        .filter(rs -> !rs.getUser().getId().equals(event.authorId()))
        // 나머지 유저에 대해 notificationService.create() 호출
        .forEach(rs -> notificationService.create(rs.getUser().getId(), title, event.content()));
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void on(RoleUpdatedEvent event) {
    String content = event.oldRole().name() + " -> " + event.newRole().name();
    notificationService.create(event.userId(), "권한이 변경되었습니다.", content);
  }

}
